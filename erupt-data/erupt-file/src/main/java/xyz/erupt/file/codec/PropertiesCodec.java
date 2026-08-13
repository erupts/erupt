package xyz.erupt.file.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.file.annotation.EruptFile;
import xyz.erupt.file.annotation.FileType;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Java {@code .properties} file mapped to a single record — a flat settings form.
 * Values are read as strings and coerced to field types by Gson downstream.
 * Written back one {@code key=value} line per model field, in declared order.
 *
 * @author YuePeng
 */
public class PropertiesCodec implements FileCodec {

    @Override
    public FileType type() {
        return FileType.PROPERTIES;
    }

    @Override
    public boolean accept(String path) {
        return "properties".equals(FileCodec.extension(path));
    }

    @Override
    public boolean singleton(EruptFile eruptFile) {
        return true;
    }

    @Override
    public List<JsonObject> decode(String content) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(content));
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
        JsonObject json = new JsonObject();
        for (String name : props.stringPropertyNames()) json.addProperty(name, props.getProperty(name));
        List<JsonObject> list = new ArrayList<>();
        list.add(json);
        return list;
    }

    @Override
    public String encode(EruptModel eruptModel, EruptFile eruptFile, List<JsonObject> records) {
        if (records.isEmpty()) return "";
        JsonObject json = records.get(0);
        StringBuilder sb = new StringBuilder();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            String key = fieldModel.getFieldName();
            JsonElement value = json.get(key);
            if (null == value || value.isJsonNull()) continue;
            sb.append(this.escapeKey(key)).append('=').append(this.escapeValue(FileCodec.asText(value))).append('\n');
        }
        return sb.toString();
    }

    private String escapeKey(String key) {
        return key.replace("\\", "\\\\").replace("=", "\\=").replace(":", "\\:").replace(" ", "\\ ");
    }

    private String escapeValue(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

}
