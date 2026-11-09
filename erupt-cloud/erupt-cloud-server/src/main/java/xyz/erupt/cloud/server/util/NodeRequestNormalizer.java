package xyz.erupt.cloud.server.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.R;
import xyz.erupt.cloud.server.service.NodeDrillService;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.linq.lambda.LambdaSee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The values a v2 node receives (PROTOCOL.md §12.4), made from a request the server has already checked
 * ({@code RemoteRequestValidator}): every value as the JSON type its field stores, every condition with its
 * effective operator and a value of the shape that operator takes, a link tree's picked keys as an {@code IN}
 * condition. Normalizing a normalized request changes nothing.
 *
 * @author YuePeng
 * date 2026/11/05
 */
public final class NodeRequestNormalizer {

    // "number" for a numeric field, the type's simple name otherwise (PROTOCOL.md §7.1)
    private static final String NUMBER = "number";

    private static final String DEFAULT_KEY = "id";

    private static final Set<EditType> REFERENCES = EnumSet.of(EditType.REFERENCE_TABLE, EditType.REFERENCE_TREE);

    private static final Set<EditType> NUMBERS = EnumSet.of(EditType.NUMBER, EditType.RATE, EditType.SLIDER);

    private static final Set<QueryExpression> NO_VALUE = EnumSet.of(QueryExpression.NULL, QueryExpression.NOT_NULL);

    private static final Set<QueryExpression> LISTS = EnumSet.of(QueryExpression.IN, QueryExpression.NOT_IN);

    // the types a record holds a list of values for, each value a scalar
    private static final Set<EditType> SCALAR_LISTS = EnumSet.of(EditType.MULTI_CHOICE, EditType.TAGS, EditType.TRANSFER);

    // the types a record holds rows of another erupt for, as the node stores them (§13)
    private static final Set<EditType> ROWS = EnumSet.of(EditType.TAB_TABLE_ADD, EditType.TAB_TABLE_REFER, EditType.MULTI_FORM);

    // what a table query carries to a node: the paging, the checked conditions and sort, the view and the server's drill
    private static final Set<String> QUERY_MEMBERS = Set.of(LambdaSee.field(TableQuery::getPageIndex), LambdaSee.field(TableQuery::getPageSize),
            LambdaSee.field(TableQuery::getSort), LambdaSee.field(TableQuery::getCondition), LambdaSee.field(TableQuery::getVis), NodeDrillService.DRILL);

    private static final String CONDITION = LambdaSee.field(TableQuery::getCondition);

    private static final String LINK_TREE_VAL = LambdaSee.field(TableQuery::getLinkTreeVal);

    private static final String KEY = LambdaSee.field(Condition::getKey);

    private static final String EXPRESSION = LambdaSee.field(Condition::getExpression);

    private static final String VALUE = LambdaSee.field(Condition::getValue);

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final Pattern YEAR = Pattern.compile("\\d{4}");

    // the fraction of a second a picker may add, dropped; a zone is refused rather than dropped
    private static final Pattern FRACTION = Pattern.compile("\\.\\d+");

    private static final String ILLEGAL_CONDITION = "Illegal search condition";

    private NodeRequestNormalizer() {
    }

    /**
     * @param builds the build of another erupt on the same node, by its local name, for the key type of a reference
     */
    public static JsonObject query(EruptBuildModel build, JsonObject query, Function<String, EruptBuildModel> builds) {
        Map<String, Field> fields = fields(build);
        JsonObject normalized = new JsonObject();
        for (Map.Entry<String, JsonElement> member : query.entrySet()) {
            if (QUERY_MEMBERS.contains(member.getKey())) normalized.add(member.getKey(), member.getValue().deepCopy());
        }
        JsonArray conditions = new JsonArray();
        JsonElement sent = query.get(CONDITION);
        if (null != sent && sent.isJsonArray()) {
            for (JsonElement element : sent.getAsJsonArray()) {
                if (!element.isJsonObject()) throw illegalCondition();
                JsonObject condition = condition(fields, element.getAsJsonObject(), builds);
                if (null != condition) conditions.add(condition);
            }
        }
        // the keys picked in the tree beside the table: the rows whose link tree field holds one of them
        LinkTree linkTree = erupt(build).linkTree();
        JsonArray picked = picked(query);
        if (StringUtils.isNotBlank(linkTree.field()) && !picked.isEmpty()) {
            JsonObject in = new JsonObject();
            in.addProperty(KEY, linkTree.field());
            in.addProperty(EXPRESSION, QueryExpression.IN.name());
            in.add(VALUE, picked);
            JsonObject condition = condition(fields, in, builds);
            if (null != condition) conditions.add(condition);
        } else if (StringUtils.isNotBlank(linkTree.field()) && linkTree.dependNode()) {
            // a table that depends on a picked node, with none picked, lists nothing: no row has a null primary key
            JsonObject none = new JsonObject();
            none.addProperty(KEY, erupt(build).primaryKeyCol());
            none.addProperty(EXPRESSION, QueryExpression.NULL.name());
            if (!conditions.contains(none)) conditions.add(none);
        }
        normalized.add(CONDITION, conditions);
        return normalized;
    }

    /**
     * The fields the model declares and its primary key, each as its field stores it; anything else is dropped.
     */
    public static JsonObject record(EruptBuildModel build, JsonObject record, Function<String, EruptBuildModel> builds) {
        Map<String, Field> fields = fields(build);
        String pk = erupt(build).primaryKeyCol();
        JsonObject normalized = new JsonObject();
        JsonElement key = record.get(pk);
        if (null != key && !key.isJsonNull()) normalized.add(pk, key(build, key));
        for (Field field : fields.values()) {
            String name = field.model().getFieldName();
            if (name.equals(pk) || !record.has(name)) continue;
            normalized.add(name, value(field, record.get(name), builds));
        }
        return normalized;
    }

    /**
     * A primary key typed like the model's primary key column: a number for a numeric one, a string otherwise.
     */
    public static JsonElement key(EruptBuildModel build, JsonElement key) {
        if (null == key || key.isJsonNull()) return JsonNull.INSTANCE;
        if (!key.isJsonPrimitive()) throw illegalCondition();
        Field pk = fields(build).get(erupt(build).primaryKeyCol());
        if (null != pk && NUMBER.equals(pk.model().getFieldReturnName())) {
            try {
                return new JsonPrimitive(new BigDecimal(key.getAsString().trim()));
            } catch (NumberFormatException e) {
                throw illegalCondition();
            }
        }
        return new JsonPrimitive(key.getAsString());
    }

    // null when the condition matches everything (an empty value), and is left out as a local erupt leaves it out
    private static JsonObject condition(Map<String, Field> fields, JsonObject condition, Function<String, EruptBuildModel> builds) {
        JsonElement keyElement = condition.get(KEY);
        if (null == keyElement || !keyElement.isJsonPrimitive()) throw illegalCondition();
        String key = keyElement.getAsString();
        Field field = fields.get(key);
        QueryExpression expression = expression(field, condition.get(EXPRESSION));
        JsonElement value = condition.get(VALUE);
        JsonObject normalized = new JsonObject();
        normalized.addProperty(KEY, key);
        normalized.addProperty(EXPRESSION, expression.name());
        if (NO_VALUE.contains(expression)) return normalized;
        if (empty(value)) return null;
        // a condition the server added on a field the build does not carry: kept as sent
        if (null == field) {
            normalized.add(VALUE, value);
            return normalized;
        }
        if (expression == QueryExpression.RANGE) {
            if (!value.isJsonArray() || value.getAsJsonArray().size() != 2) throw illegalCondition();
            JsonArray range = new JsonArray();
            for (JsonElement bound : value.getAsJsonArray()) {
                if (empty(bound)) throw illegalCondition();
                range.add(scalar(field, bound, builds));
            }
            normalized.add(VALUE, range);
        } else if (LISTS.contains(expression)) {
            JsonArray list = new JsonArray();
            for (JsonElement item : value.isJsonArray() ? value.getAsJsonArray() : single(value)) {
                if (!empty(item)) list.add(scalar(field, item, builds));
            }
            if (list.isEmpty()) return null;
            normalized.add(VALUE, list);
        } else {
            if (value.isJsonArray()) throw illegalCondition();
            // LIKE takes the text as typed: the node adds the wildcards and escapes it
            if (expression == QueryExpression.LIKE || expression == QueryExpression.NOT_LIKE) {
                if (!value.isJsonPrimitive()) throw illegalCondition();
                normalized.add(VALUE, new JsonPrimitive(value.getAsString()));
            } else {
                normalized.add(VALUE, scalar(field, value, builds));
            }
        }
        return normalized;
    }

    // The operator a local erupt would run (EruptUtil) when the request names none: the field's own. A named one is kept,
    // since the conditions the server adds (a drill's, a link tree's, a dependent picker's) name theirs
    private static QueryExpression expression(Field field, JsonElement sent) {
        QueryExpression expression = null;
        if (null != sent && sent.isJsonPrimitive()) {
            try {
                expression = QueryExpression.valueOf(sent.getAsString());
            } catch (IllegalArgumentException e) {
                throw illegalCondition();
            }
        }
        if (null != field && (null == expression || expression == QueryExpression.AUTO)) {
            QueryExpression operator = field.edit().search().operator();
            // AUTO as SearchProxy resolves it: a range for dates, an exact match for everything else
            expression = operator != QueryExpression.AUTO ? operator : field.edit().type() == EditType.DATE ? QueryExpression.RANGE : QueryExpression.EQ;
        }
        if (null == expression || expression == QueryExpression.AUTO) throw illegalCondition();
        return expression;
    }

    // A record's value: a list for the types that hold several, one value otherwise
    private static JsonElement value(Field field, JsonElement value, Function<String, EruptBuildModel> builds) {
        if (empty(value)) return JsonNull.INSTANCE;
        EditType type = field.edit().type();
        if (type == EditType.CHECKBOX || type == EditType.TAB_TREE) {
            // the browser submits [{id: key}] / [{<pk>: key}]: the keys alone
            JsonArray keys = new JsonArray();
            String keyName = type == EditType.CHECKBOX ? DEFAULT_KEY : targetPk(field, builds);
            for (JsonElement item : value.isJsonArray() ? value.getAsJsonArray() : single(value)) {
                JsonElement key = item.isJsonObject() ? item.getAsJsonObject().get(keyName) : item;
                if (!empty(key)) keys.add(targetKey(field, key, builds));
            }
            return keys;
        }
        if (ROWS.contains(type) || (type == EditType.COMBINE && value.isJsonObject())) {
            // sub-table rows, a combined form: as the node stores them
            return value;
        }
        if (SCALAR_LISTS.contains(type) && value.isJsonArray()) {
            // multi choice, transfer, tags as a list: each value a scalar, never an object a node could read as a query
            JsonArray values = new JsonArray();
            for (JsonElement item : value.getAsJsonArray()) {
                if (!item.isJsonPrimitive()) throw incorrectFormat(field.edit());
                values.add(item);
            }
            return values;
        }
        return scalar(field, value, builds);
    }

    private static JsonElement scalar(Field field, JsonElement value, Function<String, EruptBuildModel> builds) {
        Edit edit = field.edit();
        EditType type = edit.type();
        if (REFERENCES.contains(type)) {
            String keyName = type == EditType.REFERENCE_TABLE ? edit.referenceTableType().id() : edit.referenceTreeType().id();
            JsonElement key = value.isJsonObject() ? value.getAsJsonObject().get(StringUtils.defaultIfBlank(keyName, DEFAULT_KEY)) : value;
            return empty(key) ? JsonNull.INSTANCE : targetKey(field, key, builds);
        }
        // a node builds its query from these: an object or a list where one value goes is refused, never passed on
        if (!value.isJsonPrimitive()) throw incorrectFormat(edit);
        String text = value.getAsString();
        if (NUMBERS.contains(type)) {
            if (StringUtils.isBlank(text)) return JsonNull.INSTANCE;
            try {
                return new JsonPrimitive(new BigDecimal(text.trim()));
            } catch (NumberFormatException e) {
                throw new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.must.number")));
            }
        }
        if (type == EditType.BOOLEAN) {
            if (value.getAsJsonPrimitive().isBoolean()) return value;
            if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) return new JsonPrimitive(Boolean.parseBoolean(text));
            throw incorrectFormat(edit);
        }
        if (type == EditType.DATE) return new JsonPrimitive(date(edit, text.trim()));
        return new JsonPrimitive(text);
    }

    // The wire format of each date type; WEEK and QUARTER, and a type the node left AUTO, travel as the picker wrote them
    private static String date(Edit edit, String text) {
        DateType.Type type = edit.dateType().type();
        try {
            return switch (type) {
                case DATE -> LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text).toString();
                case DATE_TIME -> LocalDateTime.parse(dateTime(text), DATE_TIME).format(DATE_TIME);
                case TIME -> LocalTime.parse(text.length() == 5 ? text + ":00" : text, TIME).format(TIME);
                case MONTH -> YearMonth.parse(text.length() > 7 ? text.substring(0, 7) : text, MONTH).format(MONTH);
                case YEAR -> {
                    String year = text.length() > 4 ? text.substring(0, 4) : text;
                    if (!YEAR.matcher(year).matches()) throw incorrectFormat(edit);
                    yield year;
                }
                default -> text;
            };
        } catch (DateTimeParseException e) {
            throw incorrectFormat(edit);
        }
    }

    // yyyy-MM-dd[( |T)HH:mm[:ss[.SSS]]] as yyyy-MM-dd HH:mm:ss
    private static String dateTime(String text) {
        String value = text.replace('T', ' ');
        if (value.length() == 10) return value + " 00:00:00";
        if (value.length() == 16) return value + ":00";
        if (value.length() > 19 && FRACTION.matcher(value.substring(19)).matches()) return value.substring(0, 19);
        return value;
    }

    private static JsonElement targetKey(Field field, JsonElement key, Function<String, EruptBuildModel> builds) {
        EruptBuildModel target = target(field, builds);
        return null == target ? key : key(target, key);
    }

    private static String targetPk(Field field, Function<String, EruptBuildModel> builds) {
        EruptBuildModel target = target(field, builds);
        return null == target ? DEFAULT_KEY : erupt(target).primaryKeyCol();
    }

    // The erupt a reference points at, on the same node; null when the node does not serve it
    private static EruptBuildModel target(Field field, Function<String, EruptBuildModel> builds) {
        try {
            return builds.apply(field.model().getFieldReturnName());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static JsonArray picked(JsonObject query) {
        JsonElement picked = query.get(LINK_TREE_VAL);
        JsonArray keys = new JsonArray();
        if (null == picked || picked.isJsonNull()) return keys;
        for (JsonElement key : picked.isJsonArray() ? picked.getAsJsonArray() : single(picked)) {
            if (!empty(key)) keys.add(key);
        }
        return keys;
    }

    private static JsonArray single(JsonElement value) {
        JsonArray array = new JsonArray();
        array.add(value);
        return array;
    }

    private static boolean empty(JsonElement value) {
        return null == value || value.isJsonNull() || (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                && value.getAsString().isEmpty()) || (value.isJsonArray() && value.getAsJsonArray().isEmpty());
    }

    private static EruptApiErrorTip illegalCondition() {
        return new EruptApiErrorTip(R.error(I18nTranslate.$translate(ILLEGAL_CONDITION)));
    }

    private static EruptApiErrorTip incorrectFormat(Edit edit) {
        return new EruptApiErrorTip(R.error(edit.title() + " " + I18nTranslate.$translate("erupt.incorrect_format")));
    }

    private static Erupt erupt(EruptBuildModel build) {
        return AnnotationProcess.jsonToAnnotation(Erupt.class, Optional.ofNullable(build.getEruptModel().getEruptJson()).orElseGet(JsonObject::new));
    }

    private static Map<String, Field> fields(EruptBuildModel build) {
        Map<String, Field> fields = new LinkedHashMap<>();
        for (EruptFieldModel model : Optional.ofNullable(build.getEruptModel().getEruptFieldModels()).orElse(List.of())) {
            if (null == model.getEruptFieldJson()) continue;
            fields.put(model.getFieldName(), new Field(model, AnnotationProcess.jsonToAnnotation(EruptField.class, model.getEruptFieldJson())));
        }
        return fields;
    }

    private record Field(EruptFieldModel model, EruptField annotation) {
        Edit edit() {
            return annotation.edit();
        }
    }

}
