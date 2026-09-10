package xyz.erupt.designer.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.designer.store.DesignerStore;
import xyz.erupt.jdbc.support.JdbcModelTable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Row data layer for designer models: one SQLite table per design (see {@link DesignerStore}),
 * filtering / sorting / paging pushed down as SQL through {@link JdbcModelTable}.
 * <p>
 * Scalar fields are native columns. Reference fields ({@code {id,label,...}}) and multi-value
 * fields are JSON text columns queried with SQLite's json_extract / json_each, so reference
 * search by id and multi-choice search by value both run in the database.
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
    private DesignerStore store;

    private final JdbcModelTable table = new JdbcModelTable(
            model -> store.getTemplate(), model -> DesignerStore.tableName(model.getEruptName())) {

        // reference columns sort by their label
        @Override
        protected String column(EruptModel model, EruptFieldModel field) {
            if (Map.class.isAssignableFrom(field.getField().getType())) {
                return "json_extract(" + field.getFieldName() + ", '$.label')";
            }
            return field.getFieldName();
        }

        @Override
        protected String condition(EruptModel model, EruptFieldModel field, Condition condition, String p, Map<String, Object> params) {
            Class<?> type = field.getField().getType();
            String name = field.getFieldName();
            if (Map.class.isAssignableFrom(type)) {
                // reference search arrives as the linked row id
                String id = "json_extract(" + name + ", '$.id')";
                return switch (condition.getExpression()) {
                    case EQ -> bind(params, p, numeric(condition.getValue()), id + " = :" + p);
                    case NEQ -> bind(params, p, numeric(condition.getValue()), id + " <> :" + p);
                    case IN -> bind(params, p, numeric(condition.getValue()), id + " in (:" + p + ")");
                    case NOT_IN -> bind(params, p, numeric(condition.getValue()), id + " not in (:" + p + ")");
                    case NULL -> name + " is null";
                    case NOT_NULL -> name + " is not null";
                    default -> null;
                };
            }
            if (Collection.class.isAssignableFrom(type)) {
                // multi-value search matches any element of the JSON array
                String each = "exists (select 1 from json_each(" + model.getEruptName() + "." + name + ") where json_each.value ";
                return switch (condition.getExpression()) {
                    case EQ -> bind(params, p, condition.getValue(), each + "= :" + p + ")");
                    case NEQ -> "not " + bind(params, p, condition.getValue(), each + "= :" + p + ")");
                    case IN -> bind(params, p, condition.getValue(), each + "in (:" + p + "))");
                    case NOT_IN -> "not " + bind(params, p, condition.getValue(), each + "in (:" + p + "))");
                    case NULL -> name + " is null";
                    case NOT_NULL -> name + " is not null";
                    default -> null;
                };
            }
            return null;
        }
    };

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return table.findById(eruptModel, id);
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        return table.query(eruptModel, page, eruptQuery);
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return table.queryColumn(eruptModel, columns, eruptQuery);
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        table.insert(eruptModel, object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        table.update(eruptModel, object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        table.delete(eruptModel, object);
    }

    private static String bind(Map<String, Object> params, String p, Object value, String sql) {
        params.put(p, value);
        return sql;
    }

    // ids are stored as JSON integers; bind numeric-looking client values as numbers so SQLite compares them numerically
    private static Object numeric(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(DesignerDataService::numeric).collect(Collectors.toList());
        }
        if (value instanceof String text && text.matches("-?\\d+")) return Long.valueOf(text);
        return value;
    }

}
