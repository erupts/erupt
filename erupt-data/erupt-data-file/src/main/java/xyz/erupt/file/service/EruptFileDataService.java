package xyz.erupt.file.service;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;
import xyz.erupt.file.codec.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * File-backed data source: the model binds via {@link EruptFile} to a CSV, JSON,
 * YAML, properties or markdown file, dispatched by extension through a
 * {@link FileCodec}. The file is re-read on every query and rewritten as a whole
 * on every write. Filtering, sorting and paging come from the base class. Writes
 * to the same file are serialized with a per-path lock; external edits are picked
 * up on the next query since nothing is cached.
 * <p>
 * Single-record formats (properties, markdown, and JSON / YAML with
 * {@code single = true}) hold exactly one row: add seeds it, edit overwrites it,
 * delete clears it — no primary-key bookkeeping.
 *
 * @author YuePeng
 */
@Service
public class EruptFileDataService extends EruptBeanDataService<Object> {

    public static final String DATA_PROCESSOR = "FILE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptFileDataService.class);
    }

    // JSON claims every unmatched extension, so it must stay last
    private static final List<FileCodec> CODECS = buildCodecs();

    private final Map<String, Object> fileLocks = new ConcurrentHashMap<>();

    private static List<FileCodec> buildCodecs() {
        List<FileCodec> codecs = new ArrayList<>();
        codecs.add(new CsvCodec());
        codecs.add(new TsvCodec());
        // YAML lights up only when SnakeYAML is present, without a hard dependency
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
            codecs.add((FileCodec) Class.forName("xyz.erupt.file.codec.YamlCodec").getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException ignore) {
        }
        codecs.add(new PropertiesCodec());
        codecs.add(new IniCodec());
        codecs.add(new MarkdownCodec());
        codecs.add(new XmlCodec());
        codecs.add(new JsonlCodec());
        // JSON is the fallback (accepts every path), so it must be registered last
        codecs.add(new JsonCodec());
        return codecs;
    }

    @Override
    protected List<Object> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        return this.load(eruptModel, this.eruptFile(eruptModel));
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptFile eruptFile = this.eruptFile(eruptModel);
        if (this.codec(eruptFile).singleton(eruptFile)) {
            this.write(eruptModel, eruptFile, list -> {
                if (!list.isEmpty()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.single_exists"));
                list.add(object);
            });
            return;
        }
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        this.write(eruptModel, eruptFile, list -> {
            Object id = this.readValue(eruptModel, object, primaryKey);
            if (null == id) {
                this.generatePrimaryKey(eruptModel, object, list);
            } else if (list.stream().anyMatch(bean -> this.eq(this.readValue(eruptModel, bean, primaryKey), id))) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.key_exists") + " → " + id);
            }
            list.add(object);
        });
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptFile eruptFile = this.eruptFile(eruptModel);
        if (this.codec(eruptFile).singleton(eruptFile)) {
            this.write(eruptModel, eruptFile, list -> {
                list.clear();
                list.add(object);
            });
            return;
        }
        this.write(eruptModel, eruptFile, list -> {
            Object id = this.requireId(eruptModel, object);
            for (int i = 0; i < list.size(); i++) {
                if (this.eq(this.readValue(eruptModel, list.get(i), eruptModel.getErupt().primaryKeyCol()), id)) {
                    list.set(i, object);
                    return;
                }
            }
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.row_not_found"));
        });
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptFile eruptFile = this.eruptFile(eruptModel);
        if (this.codec(eruptFile).singleton(eruptFile)) {
            this.write(eruptModel, eruptFile, List::clear);
            return;
        }
        this.write(eruptModel, eruptFile, list -> {
            Object id = this.requireId(eruptModel, object);
            if (!list.removeIf(bean -> this.eq(this.readValue(eruptModel, bean, eruptModel.getErupt().primaryKeyCol()), id))) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.row_not_found"));
            }
        });
    }

    private void write(EruptModel eruptModel, EruptFile eruptFile, Consumer<List<Object>> mutation) {
        synchronized (fileLocks.computeIfAbsent(eruptFile.value(), k -> new Object())) {
            List<Object> list = this.load(eruptModel, eruptFile);
            mutation.accept(list);
            this.save(eruptModel, eruptFile, list);
        }
    }

    private List<Object> load(EruptModel eruptModel, EruptFile eruptFile) {
        Path path = Paths.get(eruptFile.value());
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) return new ArrayList<>();
            return this.codec(eruptFile).decode(content, eruptFile).stream()
                    .map(json -> GsonFactory.getGson().fromJson(json, eruptModel.getClazz()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.io_error") + " → " + e.getMessage());
        }
    }

    private void save(EruptModel eruptModel, EruptFile eruptFile, List<Object> list) {
        Path path = Paths.get(eruptFile.value());
        try {
            if (null != path.getParent()) Files.createDirectories(path.getParent());
            List<JsonObject> records = list.stream()
                    .map(bean -> GsonFactory.getGson().toJsonTree(bean).getAsJsonObject())
                    .collect(Collectors.toList());
            Files.writeString(path, this.codec(eruptFile).encode(eruptModel, eruptFile, records), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.io_error") + " → " + e.getMessage());
        }
    }

    private FileCodec codec(EruptFile eruptFile) {
        if (FileType.AUTO != eruptFile.type()) {
            return CODECS.stream().filter(codec -> codec.type() == eruptFile.type()).findFirst()
                    .orElseThrow(() -> new EruptWebApiRuntimeException(
                            I18nTranslate.$translate("file.unsupported_type") + " → " + eruptFile.type()));
        }
        return CODECS.stream().filter(codec -> codec.accept(eruptFile.value())).findFirst()
                .orElseThrow(() -> new EruptWebApiRuntimeException("No file codec for " + eruptFile.value()));
    }

    private Object requireId(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.primary_key_missing"));
        return id;
    }

    @SneakyThrows
    private void generatePrimaryKey(EruptModel eruptModel, Object object, List<Object> list) {
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        Field field = null;
        for (Class<?> c = object.getClass(); null != c && c != Object.class; c = c.getSuperclass()) {
            try {
                field = c.getDeclaredField(primaryKey);
                break;
            } catch (NoSuchFieldException ignore) {
            }
        }
        if (null == field) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("file.primary_key_missing"));
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (type == Long.class || type == long.class || type == Integer.class || type == int.class) {
            long next = list.stream().map(bean -> this.readValue(eruptModel, bean, primaryKey))
                    .filter(value -> value instanceof Number)
                    .mapToLong(value -> ((Number) value).longValue()).max().orElse(0) + 1;
            field.set(object, type == Long.class || type == long.class ? next : (int) next);
        } else {
            field.set(object, UUID.randomUUID().toString());
        }
    }

    private EruptFile eruptFile(EruptModel eruptModel) {
        EruptFile eruptFile = eruptModel.getClazz().getAnnotation(EruptFile.class);
        if (null == eruptFile) {
            throw new EruptWebApiRuntimeException("@EruptFile annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptFile;
    }

}
