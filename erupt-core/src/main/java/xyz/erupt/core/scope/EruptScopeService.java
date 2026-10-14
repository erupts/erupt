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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime of {@link EruptScope}. The picked values travel as plain JSON in the {@value #HEADER}
 * request header, {@code {"KubeScope":{"cluster":"orbstack"}}}, URL-encoded. The header is client
 * input: only the scope the requested model declares is read, and its values are checked against the
 * options the current user is offered on every request. A check holds for a minute per user and
 * values, so a page of requests asks the option handlers once.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptScopeService {

    public static final String HEADER = "erupt-scope";

    private static final String VARS_KEY = EruptScopeService.class.getName();

    private static final Set<EditType> SCOPE_TYPES = EnumSet.of(EditType.CHOICE, EditType.MULTI_CHOICE);

    // How long a passed check is trusted: also how long a revoked option keeps working
    private static final long CHECK_TTL_MILLIS = 60_000;

    private static final int MAX_CHECKS = 10_000;

    private final Gson gson = GsonFactory.getGson();

    // user | scope | values -> when the check expires
    private final Map<String, Long> checked = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------ declaration

    public static EruptScope scopeOf(EruptModel eruptModel) {
        return null == eruptModel ? null : eruptModel.getClazz().getAnnotation(EruptScope.class);
    }

    public static EruptModel scopeModel(Class<?> scope) {
        EruptModel model = EruptCoreService.getErupt(scope.getSimpleName());
        if (null == model) throw new EruptWebApiRuntimeException("Scope is not an erupt model: " + scope.getSimpleName());
        return model;
    }

    // The scope's fields that take part: only choices can be checked against what the user may pick
    public static List<EruptFieldModel> scopeFields(EruptModel scopeModel) {
        return scopeModel.getEruptFieldModels().stream().filter(it -> SCOPE_TYPES.contains(it.getEruptField().edit().type())).toList();
    }

    // Model fields bound to the scope: same name as a scope field
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
     * The current request's values of a scope: the checked header values, else the defaults.
     */
    public JsonObject current(Class<?> scope) {
        Map<String, JsonObject> resolved = this.resolved();
        JsonObject values = resolved.get(scope.getSimpleName());
        if (null == values) {
            values = this.defaults(scope);
            resolved.put(scope.getSimpleName(), values);
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
     * What a user who never picked sees: the form's addBehavior values, and the first option of a
     * required choice left empty.
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
        JsonObject values = gson.toJsonTree(instance).getAsJsonObject();
        for (EruptFieldModel field : scopeFields(model)) {
            JsonElement value = values.get(field.getFieldName());
            if (field.getEruptField().edit().notNull() && empty(value)) {
                Object current = gson.fromJson(values, scope);
                List<VLModel> options = EruptUtil.getChoiceListFilter(model, field, current);
                if (!options.isEmpty()) values.addProperty(field.getFieldName(), options.get(0).getValue());
            }
        }
        return this.only(model, values);
    }

    // ------------------------------------------------------------------ accepting a pick

    public record Accepted(JsonObject values, Map<String, String> labels) {
    }

    /**
     * Checks a scope pick the way an add is checked, plus every choice against the options the
     * current user is offered; answers the values kept and their labels.
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
        long now = System.currentTimeMillis();
        String key = MetaContext.getUser().getUid() + "|" + scope.getSimpleName() + "|" + values;
        Long expireAt = checked.get(key);
        if (null != expireAt && expireAt > now) return true;
        try {
            this.accept(scope, values);
        } catch (EruptWebApiRuntimeException e) {
            return false;
        }
        if (checked.size() > MAX_CHECKS) checked.values().removeIf(it -> it < now);
        checked.put(key, now + CHECK_TTL_MILLIS);
        return true;
    }

    // ------------------------------------------------------------------ binding

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
            Class<?> type = eruptModel.getEruptFieldMap().get(field).getField().getType();
            if (value.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                value.getAsJsonArray().forEach(it -> list.add(TypeUtil.typeStrConvertObject(it.getAsString(), type)));
                conditions.add(new Condition(field, list, QueryExpression.IN));
            } else {
                conditions.add(new Condition(field, TypeUtil.typeStrConvertObject(value.getAsString(), type), QueryExpression.EQ));
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
            if (!value.isJsonArray()) {
                data.add(field, value);
                continue;
            }
            JsonElement submitted = data.get(field);
            if (empty(submitted) || !value.getAsJsonArray().contains(submitted)) {
                String title = eruptModel.getEruptFieldMap().get(field).getEruptField().edit().title();
                throw new EruptWebApiRuntimeException(title + " " + I18nTranslate.$translate("erupt.scope.out_of_scope"));
            }
        }
    }

    /**
     * Fills a new form with the current scope, so the add form shows where the record will land.
     */
    public void prefill(EruptModel eruptModel, JsonObject data) {
        List<String> fields = boundFields(eruptModel);
        if (fields.isEmpty()) return;
        JsonObject values = this.current(scopeOf(eruptModel).value());
        for (String field : fields) {
            JsonElement value = values.get(field);
            if (!empty(value) && !value.isJsonArray()) data.add(field, value);
        }
    }

}
