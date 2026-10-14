package xyz.erupt.core.scope;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/**
 * Runtime of {@link EruptScope}. The picked values travel as signed tokens in the
 * {@value #HEADER} request header: the options are checked once, when the user submits the scope
 * form, and every later request only verifies the signature. A token names its scope, its user and
 * its expiry, so it can be neither edited nor borrowed.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptScopeService {

    public static final String HEADER = "erupt-scope";

    private static final String HMAC = "HmacSHA256";

    private static final String VARS_KEY = EruptScopeService.class.getName();

    private static final Set<EditType> SCOPE_TYPES = EnumSet.of(EditType.CHOICE, EditType.MULTI_CHOICE);

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private final Gson gson = GsonFactory.getGson();

    @Value("${erupt.scope.secret:}")
    private String secret;

    @Value("${erupt.scope.ttl-minutes:30}")
    private long ttlMinutes;

    private byte[] key;

    @PostConstruct
    void init() {
        if (StringUtils.isBlank(secret)) {
            key = new byte[32];
            new SecureRandom().nextBytes(key);
        } else {
            key = secret.getBytes(StandardCharsets.UTF_8);
        }
    }

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
     * The current request's values of a scope: the verified header token, else the defaults.
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
        if (StringUtils.isNotBlank(header)) {
            for (String token : header.split(",")) {
                Optional.ofNullable(this.verify(token.trim())).ifPresent(it -> resolved.put(it.scope(), it.values()));
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

    public record Accepted(String token, JsonObject values, Map<String, String> labels, long expireAt) {
    }

    /**
     * Checks a submitted scope form the way an add is checked, plus every choice against the options
     * the current user is offered, then signs it.
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
        long expireAt = System.currentTimeMillis() / 1000 + ttlMinutes * 60;
        return new Accepted(this.sign(scope.getSimpleName(), values, expireAt), values, labels, expireAt);
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

    // ------------------------------------------------------------------ token

    private record Signed(String scope, JsonObject values) {
    }

    private String sign(String scope, JsonObject values, long expireAt) {
        JsonObject payload = new JsonObject();
        payload.addProperty("s", scope);
        payload.addProperty("u", String.valueOf(MetaContext.getUser().getUid()));
        payload.addProperty("e", expireAt);
        payload.add("v", values);
        String body = B64.encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
        return body + "." + B64.encodeToString(this.mac(body));
    }

    private Signed verify(String token) {
        int dot = token.indexOf('.');
        if (dot <= 0) return null;
        String body = token.substring(0, dot);
        byte[] signature;
        try {
            signature = Base64.getUrlDecoder().decode(token.substring(dot + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (!MessageDigest.isEqual(this.mac(body), signature)) return null;
        JsonObject payload = JsonParser.parseString(new String(Base64.getUrlDecoder().decode(body), StandardCharsets.UTF_8)).getAsJsonObject();
        if (payload.get("e").getAsLong() < System.currentTimeMillis() / 1000) return null;
        if (!payload.get("u").getAsString().equals(String.valueOf(MetaContext.getUser().getUid()))) return null;
        return new Signed(payload.get("s").getAsString(), payload.getAsJsonObject("v"));
    }

    private byte[] mac(String body) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
