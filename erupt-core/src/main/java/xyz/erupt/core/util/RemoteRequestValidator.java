package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Checks a request against a model known only by its build model (an erupt-cloud node's erupt), before the
 * server forwards it: conditions only on searchable fields, sorts only on sortable columns, and records held
 * to the rules a local erupt gets, with the same messages: the not-null / XSS / number / length rules of
 * {@code EruptUtil.validateEruptValue} here, and every declared limit through {@link EruptValueValidator}.
 * The field JSON is read back as {@link EruptField} through {@link AnnotationProcess#jsonToAnnotation}, so
 * the rules read the annotation as a local erupt's do. An edit is checked as submitted (the server does not
 * hold the stored record, so an unchanged out-of-range value is not exempt); sub-table rows are left to the node.
 *
 * @author YuePeng
 * date 2026/10/14
 */
public final class RemoteRequestValidator {

    // A page larger than every size the model offers, and than this, is refused
    public static final int MAX_PAGE_SIZE = 1000;

    // a reference is matched by its key, never by text
    private static final Set<QueryExpression> REFERENCE_EXPRESSIONS = EnumSet.of(QueryExpression.EQ, QueryExpression.NEQ,
            QueryExpression.IN, QueryExpression.NOT_IN, QueryExpression.NULL, QueryExpression.NOT_NULL);

    private static final Set<EditType> REFERENCES = EnumSet.of(EditType.REFERENCE_TABLE, EditType.REFERENCE_TREE);

    private static final String EXPRESSION = LambdaSee.field(Condition::getExpression);

    // i18n keys of erupt-cloud-server, English text as the convention is
    private static final String ILLEGAL_CONDITION = "Illegal search condition";

    private static final String ILLEGAL_SORT = "Illegal sort field";

    private static final String PAGE_TOO_LARGE = "Page size is too large";

    private RemoteRequestValidator() {
    }

    /**
     * @return the problem, or null when the query may be forwarded
     */
    public static String validateQuery(EruptBuildModel build, JsonObject json) {
        TableQuery query;
        try {
            query = GsonFactory.getGson().fromJson(json, TableQuery.class);
        } catch (JsonParseException e) {
            return I18nTranslate.$translate(ILLEGAL_CONDITION);
        }
        Map<String, Field> fields = fields(build);
        List<Condition> conditions = Optional.ofNullable(query.getCondition()).orElse(List.of());
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);
            Field field = null == condition ? null : fields.get(condition.getKey());
            if (null == field || !field.edit().search().value()) return I18nTranslate.$translate(ILLEGAL_CONDITION);
            QueryExpression expression = condition.getExpression();
            // Gson reads an unknown constant as null, which a request that omits the operator also is
            boolean named = json.getAsJsonArray(LambdaSee.field(TableQuery::getCondition)).get(i).getAsJsonObject().has(EXPRESSION);
            if (named && (null == expression || expression == QueryExpression.AUTO
                    || (REFERENCES.contains(field.edit().type()) && !REFERENCE_EXPRESSIONS.contains(expression)))) {
                return I18nTranslate.$translate(ILLEGAL_CONDITION);
            }
        }
        Erupt erupt = AnnotationProcess.jsonToAnnotation(Erupt.class, build.getEruptModel().getEruptJson());
        for (Sort sort : Optional.ofNullable(query.getSort()).orElse(List.of())) {
            if (null == sort || null == sort.getField() || !(sort.getField().equals(erupt.primaryKeyCol()) || sortable(fields, sort.getField()))) {
                return I18nTranslate.$translate(ILLEGAL_SORT);
            }
        }
        int offered = Arrays.stream(erupt.layout().pageSizes()).max().orElse(0);
        if (null != query.getPageSize() && query.getPageSize() > Math.max(MAX_PAGE_SIZE, offered)) {
            return I18nTranslate.$translate(PAGE_TOO_LARGE);
        }
        return null;
    }

    /**
     * @return the error to answer, or null when the record may be forwarded
     */
    public static R<?> validateRecord(EruptBuildModel build, JsonObject record) {
        try {
            for (Field field : fields(build).values()) {
                JsonElement value = record.get(field.model().getFieldName());
                basics(field, value);
                // the declared limits, by the same code a local erupt goes through
                EruptValueValidator.validate(field.edit(), value, Collections.emptyList(), () -> options(field.model()));
            }
            return null;
        } catch (EruptApiErrorTip e) {
            return e.r;
        }
    }

    // The not-null / XSS / number / length rules EruptUtil.validateEruptValue applies to a local erupt
    private static void basics(Field field, JsonElement value) {
        Edit edit = field.edit();
        boolean empty = null == value || value.isJsonNull();
        if (edit.notNull() && (empty || (value.isJsonArray() && value.getAsJsonArray().isEmpty())
                || (String.class.getSimpleName().equals(field.model().getFieldReturnName()) && value.isJsonPrimitive()
                && StringUtils.isBlank(value.getAsString())))) {
            throw new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.notnull")));
        }
        if (empty || edit.title().isEmpty()) return;
        switch (edit.type()) {
            case INPUT, TEXTAREA -> {
                if (!value.isJsonPrimitive()) throw new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.incorrect_format")));
                if (SecurityUtil.xssInspect(value.getAsString())) {
                    throw new EruptApiErrorTip(R.errorDialog(edit.title() + " " + I18nTranslate.$translate("erupt.attack.xss")));
                }
                if (edit.type() == EditType.TEXTAREA && value.getAsString().length() > edit.textareaType().length()) {
                    throw new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.data.limit_length")));
                }
                String regex = edit.inputType().regex();
                if (edit.type() == EditType.INPUT && !AnnotationConst.EMPTY_STR.equals(regex)
                        && StringUtils.isNotBlank(value.getAsString()) && !Pattern.matches(regex, value.getAsString())) {
                    throw new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.incorrect_format")));
                }
            }
            case NUMBER, SLIDER -> {
                if (!value.isJsonPrimitive() || !NumberUtils.isCreatable(value.getAsString())) {
                    throw new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.must.number")));
                }
            }
            default -> {
            }
        }
    }

    // The options the build offers: choices as VLModel, tags as plain strings; null when it offers none
    private static Set<String> options(EruptFieldModel model) {
        JsonElement component = GsonFactory.getGson().toJsonTree(model.getComponentValue());
        if (!component.isJsonArray() || component.getAsJsonArray().isEmpty()) return null;
        Set<String> options = new HashSet<>();
        for (JsonElement option : component.getAsJsonArray()) {
            if (option.isJsonPrimitive()) {
                options.add(option.getAsString());
            } else if (option.isJsonObject()) {
                VLModel vl = GsonFactory.getGson().fromJson(option, VLModel.class);
                if (null != vl.getValue() && !vl.isDisable()) options.add(vl.getValue());
            }
        }
        return options;
    }

    // "field", or "field.column" for a reference column sorted by a column of the referenced erupt
    private static boolean sortable(Map<String, Field> fields, String sortField) {
        int dot = sortField.indexOf('.');
        Field field = fields.get(dot < 0 ? sortField : sortField.substring(0, dot));
        if (null == field) return false;
        for (View view : field.annotation().views()) {
            if (view.sortable() && (dot < 0 || sortField.substring(dot + 1).equals(view.column()))) return true;
        }
        return false;
    }

    private static Map<String, Field> fields(EruptBuildModel build) {
        Map<String, Field> fields = new LinkedHashMap<>();
        for (EruptFieldModel model : Optional.ofNullable(build.getEruptModel().getEruptFieldModels()).orElse(List.of())) {
            if (null == model.getEruptFieldJson()) continue;
            fields.put(model.getFieldName(), new Field(model, AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson())));
        }
        return fields;
    }

    // A field of the build model with its JSON read back as the annotation
    private record Field(EruptFieldModel model, EruptField annotation) {
        Edit edit() {
            return annotation.edit();
        }
    }

}
