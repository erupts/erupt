package xyz.erupt.monitor.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.monitor.model.EruptClassInfo;
import xyz.erupt.upms.model.EruptMenu;

import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory data source for the erupt class registry page: rows are built from
 * EruptCoreService on every query so runtime-registered models show up instantly.
 *
 * @author YuePeng
 */
@Service
public class EruptClassInfoDataService implements IEruptDataService {

    public static final String DATA_PROCESSOR = "EruptClassInfo";

    // Trailing build-output segments stripped when resolving a class source from a directory path
    private static final Set<String> BUILD_DIRS = new HashSet<>(Arrays.asList(
            "classes", "target", "build", "out", "main", "java", "production", "bin"));

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptClassInfoDataService.class);
    }

    @Resource
    private EruptDao eruptDao;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return this.load().stream().filter(it -> it.getName().equals(id.toString())).findFirst().orElse(null);
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        List<Map<String, Object>> rows = this.load().stream().map(this::toMap)
                .filter(it -> this.match(it, eruptQuery.getConditions())).collect(Collectors.toList());
        if (null != page.getSort() && !page.getSort().isEmpty()) {
            Comparator<Map<String, Object>> comparator = null;
            for (Sort sort : page.getSort()) {
                @SuppressWarnings("unchecked")
                Comparator<Map<String, Object>> c = Comparator.comparing(it ->
                        (Comparable<Object>) it.get(sort.getField()), Comparator.nullsFirst(Comparator.naturalOrder()));
                if (sort.getDirection() == Direction.DESC) c = c.reversed();
                comparator = null == comparator ? c : comparator.thenComparing(c);
            }
            rows.sort(comparator);
        }
        page.setTotal((long) rows.size());
        int from = Math.min((page.getPageIndex() - 1) * page.getPageSize(), rows.size());
        page.setList(rows.subList(from, Math.min(from + page.getPageSize(), rows.size())));
        return page;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return this.load().stream().map(this::toMap)
                .filter(it -> this.match(it, eruptQuery.getConditions()))
                .map(it -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Column column : columns) map.put(column.getAlias(), it.get(column.getName()));
                    return map;
                }).collect(Collectors.toList());
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        throw new UnsupportedOperationException();
    }

    private List<EruptClassInfo> load() {
        Set<String> menuValues = eruptDao.lambdaQuery(EruptMenu.class).list().stream()
                .map(EruptMenu::getValue).filter(Objects::nonNull).collect(Collectors.toSet());
        List<EruptClassInfo> list = new ArrayList<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            EruptClassInfo info = new EruptClassInfo();
            info.setName(model.getEruptName());
            info.setDisplayName(model.getErupt().name());
            info.setClazz(model.getClazz().getName());
            info.setSource(this.resolveSource(model.getClazz()));
            info.setI18n(model.isI18n());
            info.setFieldCount(model.getEruptFieldModels().size());
            EruptDataProcessor processor = model.getClazz().getAnnotation(EruptDataProcessor.class);
            info.setDataProcessor(null == processor ? EruptConst.DEFAULT_DATA_PROCESSOR : processor.value());
            info.setRuntime(EruptCoreService.isRuntimeErupt(model.getEruptName()));
            info.setPublished(menuValues.contains(model.getEruptName()));
            list.add(info);
        }
        return list;
    }

    private String resolveSource(Class<?> clazz) {
        try {
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (null == codeSource || null == codeSource.getLocation()) return null;
            String path = codeSource.getLocation().getPath().replace('\\', '/');
            // nested boot jar paths keep the innermost jar last, e.g. app.jar!/BOOT-INF/lib/erupt-upms.jar!/
            int jar = path.lastIndexOf(".jar");
            if (jar > -1) {
                String head = path.substring(0, jar);
                return head.substring(head.lastIndexOf('/') + 1) + ".jar";
            }
            String[] segments = Arrays.stream(path.split("/")).filter(it -> !it.isEmpty()).toArray(String[]::new);
            int end = segments.length;
            while (end > 0 && BUILD_DIRS.contains(segments[end - 1])) end--;
            return end > 0 ? segments[end - 1] : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> toMap(EruptClassInfo info) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", info.getName());
        map.put("displayName", info.getDisplayName());
        map.put("source", info.getSource());
        map.put("clazz", info.getClazz());
        map.put("i18n", info.getI18n());
        map.put("fieldCount", info.getFieldCount());
        map.put("dataProcessor", info.getDataProcessor());
        map.put("runtime", info.getRuntime());
        map.put("published", info.getPublished());
        return map;
    }

    private boolean match(Map<String, Object> row, List<Condition> conditions) {
        if (null == conditions) return true;
        for (Condition condition : conditions) {
            Object value = row.get(condition.getKey());
            Object target = condition.getValue();
            boolean pass = switch (condition.getExpression()) {
                case LIKE -> null != value && value.toString().toLowerCase().contains(target.toString().toLowerCase());
                case EQ -> this.eq(value, target);
                case NEQ -> !this.eq(value, target);
                case IN -> target instanceof Collection
                        && ((Collection<?>) target).stream().anyMatch(it -> this.eq(value, it));
                case NULL -> null == value;
                case NOT_NULL -> null != value;
                default -> true;
            };
            if (!pass) return false;
        }
        return true;
    }

    private boolean eq(Object value, Object target) {
        if (Objects.equals(value, target)) return true;
        if (null == value || null == target) return false;
        if (value instanceof Number && target instanceof Number) {
            return ((Number) value).doubleValue() == ((Number) target).doubleValue();
        }
        return value.toString().equals(target.toString());
    }

}
