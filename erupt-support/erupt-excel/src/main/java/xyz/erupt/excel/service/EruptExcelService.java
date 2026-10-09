package xyz.erupt.excel.service;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.excel.codec.TableSheet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The format-independent half of import and export: turns a query page into a titled sheet of
 * display values, and rows of title → text back into records the pipeline can insert. Codecs
 * only encode what this produces.
 *
 * @author YuePeng
 * date 12/4/18.
 */
@Service
public class EruptExcelService {

    public static final String XLS_FORMAT = ".xls";

    public static final String XLSX_FORMAT = ".xlsx";

    // Export: every shown, exportable view column, cells rendered as the table shows them
    public TableSheet sheet(EruptModel eruptModel, Page page, List<Condition> conditions) {
        List<TableSheet.TableColumn> columns = new ArrayList<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            View[] views = fieldModel.getEruptField().views();
            for (int i = 0; i < views.length; i++) {
                if (views[i].show() && views[i].export()) {
                    columns.add(new TableSheet.TableColumn(views[i].title(), fieldModel, i));
                }
            }
        }
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> map : page.getList()) {
            List<Object> row = new ArrayList<>(columns.size());
            for (TableSheet.TableColumn column : columns) {
                row.add(this.display(eruptModel, column, map));
            }
            rows.add(row);
        }
        return new TableSheet(eruptModel, eruptModel.getErupt().name(), columns, rows, this.conditions(eruptModel, conditions), false);
    }

    // Template: the editable form fields, in the layout an import expects
    public TableSheet template(EruptModel eruptModel) {
        List<TableSheet.TableColumn> columns = new ArrayList<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.show() && !edit.readonly().add() && StringUtils.isNotBlank(edit.title())
                    && AnnotationProcess.getEditTypeMapping(edit.type()).excelOperator()) {
                columns.add(new TableSheet.TableColumn(edit.title(), fieldModel, -1));
            }
        }
        return new TableSheet(eruptModel, eruptModel.getErupt().name(), columns, List.of(), List.of(), true);
    }

    private Object display(EruptModel eruptModel, TableSheet.TableColumn column, Map<String, Object> map) {
        EruptFieldModel fieldModel = column.field();
        View view = column.view();
        Edit edit = column.edit();
        Object value = StringUtils.isNotBlank(view.column())
                ? map.get(fieldModel.getFieldName() + "_" + view.column().replace(EruptConst.DOT, "_"))
                : map.get(fieldModel.getFieldName());
        if (null == value) return null;
        String str = value.toString();
        if (edit.type() == EditType.BOOLEAN || view.type() == ViewType.BOOLEAN) {
            // the query returns the raw value; the sheet carries the wording
            if (value instanceof Boolean bool) return bool ? edit.boolType().trueText() : edit.boolType().falseText();
            if (edit.boolType().trueText().equals(str) || edit.boolType().falseText().equals(str)) return str;
            return null;
        } else if (edit.type() == EditType.CHOICE) {
            String label = EruptUtil.getChoiceMap(eruptModel, fieldModel).get(str);
            return null == label ? str : label;
        } else if (edit.type() == EditType.DATE) {
            boolean dateOnly = edit.dateType().type() == DateType.Type.DATE;
            LocalDateTime dateTime;
            if (value instanceof Date date) {
                dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            } else if (value instanceof LocalDate date) {
                return date;
            } else if (value instanceof LocalDateTime date) {
                dateTime = date;
            } else {
                return str;
            }
            return dateOnly ? dateTime.toLocalDate() : dateTime;
        }
        return str;
    }

    private List<String[]> conditions(EruptModel eruptModel, List<Condition> conditions) {
        List<String[]> list = new ArrayList<>();
        if (null == conditions) return list;
        for (Condition condition : conditions) {
            if (null == condition.getValue()) continue;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(condition.getKey());
            if (null == fieldModel) continue;
            EruptField eruptField = fieldModel.getEruptField();
            if (eruptField.views().length == 0) continue;
            list.add(new String[]{eruptField.views()[0].title(),
                    null == condition.getExpression() ? QueryExpression.EQ.name() : condition.getExpression().name(),
                    condition.getValue().toString()});
        }
        return list;
    }

    /**
     * Import: rows of edit title → cell text become records keyed by field name, with labels mapped
     * back to stored values (choice, boolean, reference) and dates normalised to ISO-8601. A column
     * that is a table column but not a form field (ID, attachments, computed views) is skipped, so
     * an export can be imported back as it is; only a title the model knows nothing about is an error.
     */
    public List<JsonObject> records(EruptModel eruptModel, List<Map<String, String>> rows) throws Exception {
        Map<String, EruptFieldModel> byTitle = new HashMap<>();
        Set<String> viewOnly = new HashSet<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            EruptField eruptField = fieldModel.getEruptField();
            if (StringUtils.isNotBlank(eruptField.edit().title())) byTitle.put(eruptField.edit().title(), fieldModel);
            for (View view : eruptField.views()) viewOnly.add(view.title());
        }
        viewOnly.removeAll(byTitle.keySet());
        Map<String, Map<String, Object>> lookups = new HashMap<>();
        List<JsonObject> records = new ArrayList<>(rows.size());
        for (Map<String, String> row : rows) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, String> cell : row.entrySet()) {
                if (viewOnly.contains(cell.getKey())) continue;
                EruptFieldModel fieldModel = byTitle.get(cell.getKey());
                if (null == fieldModel) {
                    throw new Exception(String.format(I18nTranslate.$translate("excel.unknown_column"), cell.getKey()));
                }
                String text = cell.getValue();
                if (null == text || text.isEmpty()) continue;
                Edit edit = fieldModel.getEruptField().edit();
                String name = fieldModel.getFieldName();
                switch (edit.type()) {
                    case REFERENCE_TABLE, REFERENCE_TREE -> {
                        String idKey = edit.type() == EditType.REFERENCE_TREE ? edit.referenceTreeType().id() : edit.referenceTableType().id();
                        Object id = lookups.computeIfAbsent(name, k -> this.lookup(eruptModel, fieldModel)).get(text);
                        if (null == id) throw new Exception(edit.title() + " → " + text + " not found");
                        JsonObject ref = new JsonObject();
                        ref.addProperty(idKey, id.toString());
                        json.add(name, ref);
                    }
                    case CHOICE -> {
                        Object value = lookups.computeIfAbsent(name, k -> this.lookup(eruptModel, fieldModel)).get(text);
                        if (null == value) throw new Exception(edit.title() + " → " + text + " not found");
                        json.addProperty(name, value.toString());
                    }
                    case BOOLEAN -> {
                        Object value = lookups.computeIfAbsent(name, k -> this.lookup(eruptModel, fieldModel)).get(text);
                        if (null == value) throw new Exception(edit.title() + " → " + text + " not found");
                        json.addProperty(name, (Boolean) value);
                    }
                    case DATE -> json.addProperty(name, this.isoDate(text));
                    default -> json.addProperty(name, text);
                }
            }
            if (!json.isEmpty()) records.add(json);
        }
        return records;
    }

    // Display text → stored value, for the field types whose cells are labels
    private Map<String, Object> lookup(EruptModel eruptModel, EruptFieldModel fieldModel) {
        Edit edit = fieldModel.getEruptField().edit();
        Map<String, Object> map = new HashMap<>();
        switch (edit.type()) {
            case CHOICE -> EruptUtil.getChoiceMap(eruptModel, fieldModel).forEach((value, label) -> map.put(label, value));
            case BOOLEAN -> {
                BoolType boolType = edit.boolType();
                map.put(boolType.trueText(), true);
                map.put(boolType.falseText(), false);
            }
            case REFERENCE_TABLE, REFERENCE_TREE -> {
                String id = edit.type() == EditType.REFERENCE_TREE ? edit.referenceTreeType().id() : edit.referenceTableType().id();
                String label = edit.type() == EditType.REFERENCE_TREE ? edit.referenceTreeType().label() : edit.referenceTableType().label();
                IEruptDataService dataService = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());
                List<Column> columns = List.of(new Column(id, id), new Column(label, label));
                Collection<Map<String, Object>> list = dataService.queryColumn(EruptCoreService.getErupt(fieldModel.getFieldReturnName()), columns, EruptQuery.builder().build());
                for (Map<String, Object> m : list) {
                    if (null != m.get(label)) map.put(m.get(label).toString(), m.get(id));
                }
            }
            default -> {
            }
        }
        return map;
    }

    private String isoDate(String text) throws Exception {
        try {
            return DateUtil.parseLocalDateTime(text).format(java.time.format.DateTimeFormatter.ofPattern(DateUtil.ISO_8601));
        } catch (Exception e) {
            return DateUtil.getFormatDate(DateUtil.parseDate(text), DateUtil.ISO_8601);
        }
    }

}
