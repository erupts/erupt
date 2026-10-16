package xyz.erupt.core.scope;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * Runtime of {@link EruptScope}. The picked values travel as plain JSON in the {@value #HEADER}
 * request header, {@code {"KubeScope":{"cluster":"orbstack"}}}, URL-encoded; a page opened by URL
 * (a tpl), which cannot send headers, carries the same value in the {@value #PARAM} parameter. The
 * value is client input: only the scope the requested model declares is read, and its values are
 * checked against the options the current user is offered on every request. Nothing is cached here: an option handler
 * that is costly to ask caches its own options, since only it knows how long they stay valid.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptScopeService {

    public static final String HEADER = "erupt-scope";

    public static final String PARAM = "_scope";

    private static final String VARS_KEY = EruptScopeService.class.getName();

    // Fields whose values can be checked against the options the user is offered
    private static final Set<EditType> CHOICE_TYPES = EnumSet.of(EditType.CHOICE, EditType.MULTI_CHOICE);


    private final Gson gson = GsonFactory.getGson();

    // ------------------------------------------------------------------ declaration

    public static EruptScope scopeOf(EruptModel eruptModel) {
        return null == eruptModel ? null : eruptModel.getClazz().getAnnotation(EruptScope.class);
    }

    public static EruptModel scopeModel(Class<?> scope) {
        EruptModel model = EruptCoreService.getErupt(scope.getSimpleName());
        if (null == model) throw new EruptWebApiRuntimeException("Scope is not an erupt model: " + scope.getSimpleName());
        return model;
    }

    // The scope's fields that take part: every field the form shows
    public static List<EruptFieldModel> scopeFields(EruptModel scopeModel) {
        return scopeModel.getEruptFieldModels().stream().filter(it -> StringUtils.isNotBlank(it.getEruptField().edit().title())).toList();
    }

    // Model fields bound to the scope: same name as a scope field. A reference field binds by its
    // id: the scope holds the id, the model the referenced row
    public static List<String> boundFields(EruptModel eruptModel) {
        EruptScope scope = scopeOf(eruptModel);
        if (null == scope || !scope.bind()) return Collections.emptyList();
        List<String> fields = new ArrayList<>();
        for (EruptFieldModel field : scopeFields(scopeModel(scope.value()))) {
            if (eruptModel.getEruptFieldMap().containsKey(field.getFieldName())) fields.add(field.getFieldName());
        }
        return fields;
    }

    // ------------------------------------------------------------------ current values

    /**
     * The current request's values of a scope: the checked header values, else the defaults. A
     * required field still empty refuses the request: running it unscoped would reach every scope.
     */
    public JsonObject current(Class<?> scope) {
        Map<String, JsonObject> resolved = this.resolved();
        JsonObject values = resolved.get(scope.getSimpleName());
        if (null == values) {
            values = this.defaults(scope);
            resolved.put(scope.getSimpleName(), values);
        }
        for (EruptFieldModel field : scopeFields(scopeModel(scope))) {
            Edit edit = field.getEruptField().edit();
            if (edit.notNull() && empty(values.get(field.getFieldName()))) {
                throw new EruptWebApiRuntimeException(edit.title() + " " + I18nTranslate.$translate("erupt.scope.required"));
            }
        }
        return values;
    }

    public <T> T get(Class<T> scope) {
        return gson.fromJson(this.current(scope), scope);
    }

    @SuppressWarnings("unchecked")
    private Map<String, JsonObject> resolved() {
        Map<String, Object> vars = MetaContext.getVars();
        Object cached = vars.get(VARS_KEY);
        if (cached instanceof Map) return (Map<String, JsonObject>) cached;
        Map<String, JsonObject> resolved = new HashMap<>();
        String header = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            header = attrs.getRequest().getHeader(HEADER);
            if (StringUtils.isBlank(header)) header = attrs.getRequest().getParameter(PARAM);
        }
        // Only the scope the requested model declares is read; any other key in the header is ignored
        String eruptName = MetaContext.getErupt().getName();
        EruptScope declared = null == eruptName ? null : scopeOf(EruptCoreService.getErupt(eruptName));
        if (null != declared && StringUtils.isNotBlank(header)) {
            JsonElement pick;
            try {
                pick = JsonParser.parseString(URLDecoder.decode(header, StandardCharsets.UTF_8)).getAsJsonObject()
                        .get(declared.value().getSimpleName());
            } catch (RuntimeException e) {
                pick = null;
            }
            if (null != pick && pick.isJsonObject()) {
                JsonObject values = this.only(scopeModel(declared.value()), pick.getAsJsonObject());
                // values the user may not pick are dropped: the request runs on the defaults instead
                if (this.allowed(declared.value(), values)) resolved.put(declared.value().getSimpleName(), values);
            }
        }
        vars.put(VARS_KEY, resolved);
        return resolved;
    }

    /**
     * What a user who never picked sees: the form's addBehavior values, nothing more. A required
     * field the form leaves empty stays empty, so the page asks the user instead of guessing.
     */
    public JsonObject defaults(Class<?> scope) {
        EruptModel model = scopeModel(scope);
        Object instance;
        try {
            instance = scope.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new EruptWebApiRuntimeException("Scope needs a no-arg constructor: " + scope.getSimpleName());
        }
        DataProxyInvoke.invoke(model, it -> it.addBehavior(instance));
        return this.only(model, gson.toJsonTree(instance).getAsJsonObject());
    }

    // ------------------------------------------------------------------ accepting a pick

    public record Accepted(JsonObject values, Map<String, String> labels) {
    }

    /**
     * Checks a scope pick the way an add is checked, plus every choice against the options the
     * current user is offered; other fields have no options to check and are taken as typed.
     * Answers the values kept and their labels.
     */
    public Accepted accept(Class<?> scope, JsonObject form) {
        EruptModel model = scopeModel(scope);
        JsonObject values = this.only(model, form);
        R<Void> valid = EruptUtil.validateEruptValue(model, values);
        if (!valid.isSuccess()) throw new EruptWebApiRuntimeException(valid.getMessage());
        Object current = gson.fromJson(values, scope);
        Map<String, String> labels = new LinkedHashMap<>();
        for (EruptFieldModel field : scopeFields(model)) {
            JsonElement value = values.get(field.getFieldName());
            if (empty(value)) continue;
            if (!CHOICE_TYPES.contains(field.getEruptField().edit().type())) {
                labels.put(field.getFieldName(), value.isJsonPrimitive() ? value.getAsString() : value.toString());
                continue;
            }
            Map<String, String> options = new HashMap<>();
            for (VLModel vl : EruptUtil.getChoiceListFilter(model, field, current)) options.put(vl.getValue(), vl.getLabel());
            List<String> picked = new ArrayList<>();
            if (value.isJsonArray()) value.getAsJsonArray().forEach(it -> picked.add(it.getAsString()));
            else picked.add(value.getAsString());
            for (String one : picked) {
                if (!options.containsKey(one)) {
                    throw new EruptWebApiRuntimeException(field.getEruptField().edit().title() + " " + I18nTranslate.$translate("erupt.scope.option_invalid") + " → " + one);
                }
            }
            labels.put(field.getFieldName(), String.join(", ", picked.stream().map(options::get).toList()));
        }
        return new Accepted(values, labels);
    }

    public Accepted acceptDefaults(Class<?> scope) {
        return this.accept(scope, this.defaults(scope));
    }

    // Keeps the scope fields only, so nothing else rides along in a token
    private JsonObject only(EruptModel model, JsonObject source) {
        JsonObject values = new JsonObject();
        for (EruptFieldModel field : scopeFields(model)) {
            JsonElement value = source.get(field.getFieldName());
            if (!empty(value)) values.add(field.getFieldName(), value);
        }
        return values;
    }

    private static boolean empty(JsonElement value) {
        return null == value || value.isJsonNull() || (value.isJsonArray() && value.getAsJsonArray().isEmpty())
                || (value.isJsonPrimitive() && value.getAsString().isEmpty());
    }

    // ------------------------------------------------------------------ checking the header

    private boolean allowed(Class<?> scope, JsonObject values) {
        try {
            this.accept(scope, values);
            return true;
        } catch (EruptWebApiRuntimeException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ binding

    // The id column of a reference field, null for a plain one
    private static String refId(EruptFieldModel field) {
        Edit edit = field.getEruptField().edit();
        return switch (edit.type()) {
            case REFERENCE_TABLE -> edit.referenceTableType().id();
            case REFERENCE_TREE -> edit.referenceTreeType().id();
            default -> null;
        };
    }

    // The value a bound field holds for one scope value: the value itself, or the row it refers to
    private static JsonElement bound(EruptFieldModel field, JsonElement value) {
        String refId = refId(field);
        if (null == refId) return value;
        JsonObject ref = new JsonObject();
        ref.add(refId, value);
        return ref;
    }

    // The scope value a submitted field holds: the value itself, or the id of the row it refers to
    private static JsonElement unbound(EruptFieldModel field, JsonElement submitted) {
        String refId = refId(field);
        if (null == refId || empty(submitted) || !submitted.isJsonObject()) return submitted;
        return submitted.getAsJsonObject().get(refId);
    }

    /**
     * Conditions that keep a bound model inside the current scope; empty when it has none or the
     * user picked "all".
     */
    public List<Condition> conditions(EruptModel eruptModel) {
        List<String> fields = boundFields(eruptModel);
        if (fields.isEmpty()) return Collections.emptyList();
        JsonObject values = this.current(scopeOf(eruptModel).value());
        List<Condition> conditions = new ArrayList<>();
        for (String field : fields) {
            JsonElement value = values.get(field);
            if (empty(value)) continue;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
            String refId = refId(fieldModel);
            Class<?> type = fieldModel.getField().getType();
            // a reference condition carries the id the way a search does: {id: value}
            Function<JsonElement, Object> convert = it -> null == refId
                    ? TypeUtil.typeStrConvertObject(it.getAsString(), type) : Map.of(refId, it.getAsString());
            if (value.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                value.getAsJsonArray().forEach(it -> list.add(convert.apply(it)));
                conditions.add(new Condition(field, list, QueryExpression.IN));
            } else {
                conditions.add(new Condition(field, convert.apply(value), QueryExpression.EQ));
            }
        }
        return conditions;
    }

    /**
     * Writes the current scope into a submitted add or edit, over whatever the client sent. A multi
     * pick has no single value to write: the submitted value must then be one of the picked ones.
     */
    public void bind(EruptModel eruptModel, JsonObject data) {
        List<String> fields = boundFields(eruptModel);
        if (fields.isEmpty()) return;
        JsonObject values = this.current(scopeOf(eruptModel).value());
        for (String field : fields) {
            JsonElement value = values.get(field);
            if (empty(value)) continue;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
            if (!value.isJsonArray()) {
                data.add(field, bound(fieldModel, value));
                continue;
            }
            JsonElement submitted = unbound(fieldModel, data.get(field));
            if (empty(submitted) || value.getAsJsonArray().asList().stream().noneMatch(it -> it.getAsString().equals(submitted.getAsString()))) {
                String title = fieldModel.getEruptField().edit().title();
                throw new EruptWebApiRuntimeException(title + " " + I18nTranslate.$translate("erupt.scope.out_of_scope"));
            }
        }
    }

    /**
     * Fills a new form with the current scope, so the add form shows where the record will land. A
     * reference field shows the scope option's label.
     */
    public void prefill(EruptModel eruptModel, JsonObject data) {
        List<String> fields = boundFields(eruptModel);
        if (fields.isEmpty()) return;
        Class<?> scope = scopeOf(eruptModel).value();
        JsonObject values = this.current(scope);
        Map<String, String> labels = null;
        for (String field : fields) {
            JsonElement value = values.get(field);
            if (empty(value) || value.isJsonArray()) continue;
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(field);
            JsonElement prefilled = bound(fieldModel, value);
            if (prefilled.isJsonObject()) {
                if (null == labels) labels = this.accept(scope, values).labels();
                Edit edit = fieldModel.getEruptField().edit();
                String label = edit.type() == EditType.REFERENCE_TREE ? edit.referenceTreeType().label() : edit.referenceTableType().label();
                prefilled.getAsJsonObject().addProperty(label, labels.get(field));
            }
            data.add(field, prefilled);
        }
    }

}
