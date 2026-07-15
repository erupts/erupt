package xyz.erupt.designer.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.designer.model.DesignerData;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.model.BaseModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Row data layer for designer models: each record is one JSON row in e_designer_data,
 * schema-free on any RDBMS. Conditions / sorting / paging are evaluated in memory —
 * designer models are runways, heavy data should graduate to annotation development.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Service
public class DesignerDataService implements IEruptDataService {

    static {
        DataProcessorManager.register(DesignerClassFactory.DATA_PROCESSOR, DesignerDataService.class);
    }

    @Resource
    private EruptDao eruptDao;

    private final Gson gson = GsonFactory.getGson();

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        DesignerData row = this.findRow(eruptModel, id);
        return null == row ? null : gson.fromJson(row.getData(), eruptModel.getClazz());
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        List<Map<String, Object>> rows = this.loadRows(eruptModel).stream()
                .filter(it -> this.match(it, eruptQuery.getConditions())).collect(Collectors.toList());
        if (null != page.getSort() && !page.getSort().isEmpty()) {
            Comparator<Map<String, Object>> comparator = null;
            for (Sort sort : page.getSort()) {
                @SuppressWarnings("unchecked")
                Comparator<Map<String, Object>> c = Comparator.comparing(it ->
                        (Comparable<Object>) it.get(sort.getField()), Comparator.nullsFirst(Comparator.naturalOrder()));
                if (sort.getDirection() == xyz.erupt.annotation.query.Direction.DESC) c = c.reversed();
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
        return this.loadRows(eruptModel).stream()
                .filter(it -> this.match(it, eruptQuery.getConditions()))
                .map(it -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Column column : columns) map.put(column.getAlias(), it.get(column.getName()));
                    return map;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addData(EruptModel eruptModel, Object object) {
        DesignerData row = new DesignerData();
        row.setModel(eruptModel.getEruptName());
        eruptDao.persistAndFlush(row);
        ((BaseModel) object).setId(row.getId());
        row.setData(gson.toJson(object));
        eruptDao.merge(row);
    }

    @Override
    @Transactional
    public void editData(EruptModel eruptModel, Object object) {
        DesignerData row = this.findRow(eruptModel, ((BaseModel) object).getId());
        if (null != row) {
            row.setData(gson.toJson(object));
            eruptDao.merge(row);
        }
    }

    @Override
    @Transactional
    public void deleteData(EruptModel eruptModel, Object object) {
        DesignerData row = this.findRow(eruptModel, ((BaseModel) object).getId());
        if (null != row) eruptDao.delete(row);
    }

    private DesignerData findRow(EruptModel eruptModel, Object id) {
        return eruptDao.lambdaQuery(DesignerData.class)
                .eq(DesignerData::getModel, eruptModel.getEruptName())
                .eq(DesignerData::getId, Long.valueOf(id.toString())).one();
    }

    private List<Map<String, Object>> loadRows(EruptModel eruptModel) {
        return eruptDao.lambdaQuery(DesignerData.class)
                .eq(DesignerData::getModel, eruptModel.getEruptName()).list()
                .stream().map(it -> {
                    JsonObject json = gson.fromJson(it.getData(), JsonObject.class);
                    json.addProperty("id", it.getId());
                    Map<String, Object> map = gson.fromJson(json, LinkedHashMap.class);
                    // flatten reference fields {id,label..} into field_column entries for direct table column rendering
                    new ArrayList<>(map.entrySet()).stream().filter(e -> e.getValue() instanceof Map)
                            .forEach(e -> ((Map<?, ?>) e.getValue()).forEach((k, v) ->
                                    map.put(e.getKey() + "_" + k, v)));
                    return map;
                }).collect(Collectors.toList());
    }

    private boolean match(Map<String, Object> row, List<Condition> conditions) {
        if (null == conditions) return true;
        for (Condition condition : conditions) {
            Object value = row.get(condition.getKey());
            Object target = condition.getValue();
            boolean pass = switch (condition.getExpression()) {
                case LIKE -> null != value && value.toString().contains(target.toString());
                case EQ -> this.eq(value, target);
                case NEQ -> !this.eq(value, target);
                case IN ->
                        target instanceof Collection && ((Collection<?>) target).stream().anyMatch(it -> this.eq(value, it));
                case RANGE -> {
                    List<?> range = (List<?>) target;
                    yield this.compare(value, range.get(0)) >= 0 && this.compare(value, range.get(1)) <= 0;
                }
                case GT -> this.compare(value, target) > 0;
                case GTE -> this.compare(value, target) >= 0;
                case LT -> this.compare(value, target) < 0;
                case LTE -> this.compare(value, target) <= 0;
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

    private int compare(Object value, Object target) {
        if (null == value) return -1;
        if (value instanceof Number && target instanceof Number) {
            return Double.compare(((Number) value).doubleValue(), ((Number) target).doubleValue());
        }
        return value.toString().compareTo(target.toString());
    }

}
