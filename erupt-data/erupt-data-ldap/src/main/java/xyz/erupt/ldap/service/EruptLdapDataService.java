package xyz.erupt.ldap.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.ldap.annotation.EruptLdap;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LDAP-backed data source using the JDK's built-in JNDI provider. Models annotated
 * with {@link EruptLdap} are read from and written to a directory server; field
 * names on the model are treated as LDAP attribute names (case-insensitive), and
 * the primary key column supplies the RDN value used to build each entry's DN.
 * <p>
 * Equality / LIKE / presence conditions are pushed into the LDAP search filter to
 * narrow the result set; the base class then re-evaluates every condition to
 * guarantee the same semantics as the persistent implementations (LDAP substring
 * matching is case-insensitive and locale-dependent, so we do not rely on it for
 * correctness).
 *
 * @author YuePeng
 */
@Service
public class EruptLdapDataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "LDAP";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptLdapDataService.class);
    }

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptLdap ldap = this.eruptLdap(eruptModel);
        String filter = this.buildFilter(ldap, this.mergeConditions(eruptQuery));
        SearchControls controls = this.controls(ldap);
        DirContext ctx = null;
        try {
            ctx = this.context(ldap);
            List<Map<String, Object>> rows = new ArrayList<>();
            NamingEnumeration<SearchResult> results = ctx.search(ldap.baseDn(), filter, controls);
            while (results.hasMore()) {
                rows.add(this.toMap(results.next().getAttributes()));
            }
            return rows;
        } catch (NamingException e) {
            throw this.wrap(e);
        } finally {
            this.close(ctx);
        }
    }

    // Returns a bean rather than a Map because downstream drill / edit-form logic
    // reflects on the model class of the returned object (same reason as erupt-http)
    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptLdap ldap = this.eruptLdap(eruptModel);
        DirContext ctx = null;
        try {
            ctx = this.context(ldap);
            Attributes attrs = ctx.getAttributes(this.dn(ldap, id),
                    ldap.attributes().length == 0 ? null : ldap.attributes());
            Gson gson = GsonFactory.getGson();
            return gson.fromJson(gson.toJson(this.toMap(attrs)), eruptModel.getClazz());
        } catch (NameNotFoundException e) {
            return null;
        } catch (NamingException e) {
            throw this.wrap(e);
        } finally {
            this.close(ctx);
        }
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptLdap ldap = this.eruptLdap(eruptModel);
        Object id = this.requireId(eruptModel, object);
        Attributes attrs = this.beanAttributes(eruptModel, object, ldap);
        DirContext ctx = null;
        try {
            ctx = this.context(ldap);
            ctx.createSubcontext(this.dn(ldap, id), attrs);
        } catch (NamingException e) {
            throw this.wrap(e);
        } finally {
            this.close(ctx);
        }
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptLdap ldap = this.eruptLdap(eruptModel);
        Object id = this.requireId(eruptModel, object);
        List<ModificationItem> mods = new ArrayList<>();
        String rdnAttr = ldap.rdn();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            String name = fieldModel.getFieldName();
            // RDN attribute must not be modified in place; renaming DN is a separate op
            if (name.equalsIgnoreCase(rdnAttr)) continue;
            Object value = this.readValue(eruptModel, object, name);
            Attribute attr = this.toAttribute(name, value);
            mods.add(new ModificationItem(
                    null == attr ? DirContext.REMOVE_ATTRIBUTE : DirContext.REPLACE_ATTRIBUTE,
                    null == attr ? new BasicAttribute(name) : attr));
        }
        DirContext ctx = null;
        try {
            ctx = this.context(ldap);
            ctx.modifyAttributes(this.dn(ldap, id), mods.toArray(new ModificationItem[0]));
        } catch (NamingException e) {
            throw this.wrap(e);
        } finally {
            this.close(ctx);
        }
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptLdap ldap = this.eruptLdap(eruptModel);
        Object id = this.requireId(eruptModel, object);
        DirContext ctx = null;
        try {
            ctx = this.context(ldap);
            ctx.destroySubcontext(this.dn(ldap, id));
        } catch (NamingException e) {
            throw this.wrap(e);
        } finally {
            this.close(ctx);
        }
    }

    private EruptLdap eruptLdap(EruptModel eruptModel) {
        EruptLdap ldap = eruptModel.getClazz().getAnnotation(EruptLdap.class);
        if (null == ldap) {
            throw new EruptWebApiRuntimeException("@EruptLdap annotation is missing on " + eruptModel.getEruptName());
        }
        return ldap;
    }

    private Object requireId(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id || String.valueOf(id).isEmpty()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ldap.primary_key_missing"));
        }
        return id;
    }

    private DirContext context(EruptLdap ldap) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(DirContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(DirContext.PROVIDER_URL, ldap.url());
        if (!ldap.bindDn().isEmpty()) {
            env.put(DirContext.SECURITY_AUTHENTICATION, "simple");
            env.put(DirContext.SECURITY_PRINCIPAL, ldap.bindDn());
            env.put(DirContext.SECURITY_CREDENTIALS, ldap.bindCredential());
        }
        if (ldap.timeout() > 0) {
            env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(ldap.timeout() * 1000));
            env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(ldap.timeout() * 1000));
        }
        return new InitialDirContext(env);
    }

    private SearchControls controls(EruptLdap ldap) {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setCountLimit(ldap.sizeLimit());
        if (ldap.timeout() > 0) controls.setTimeLimit(ldap.timeout() * 1000);
        if (ldap.attributes().length > 0) controls.setReturningAttributes(ldap.attributes());
        return controls;
    }

    private String dn(EruptLdap ldap, Object id) {
        return ldap.rdn() + "=" + this.escapeRdn(String.valueOf(id)) + "," + ldap.baseDn();
    }

    private Map<String, Object> toMap(Attributes attrs) throws NamingException {
        Map<String, Object> row = new LinkedHashMap<>();
        NamingEnumeration<? extends Attribute> enumeration = attrs.getAll();
        while (enumeration.hasMore()) {
            Attribute attr = enumeration.next();
            if (attr.size() == 0) continue;
            if (attr.size() == 1) {
                row.put(attr.getID(), this.decode(attr.get()));
            } else {
                List<Object> values = new ArrayList<>(attr.size());
                NamingEnumeration<?> valueEnum = attr.getAll();
                while (valueEnum.hasMore()) values.add(this.decode(valueEnum.next()));
                row.put(attr.getID(), values);
            }
        }
        return row;
    }

    private Object decode(Object value) {
        // Binary attributes come back as byte[]; treat them as UTF-8 text since erupt
        // models exposed through this data source are string / number based
        return value instanceof byte[] ? new String((byte[]) value, java.nio.charset.StandardCharsets.UTF_8) : value;
    }

    private Attributes beanAttributes(EruptModel eruptModel, Object object, EruptLdap ldap) {
        if (ldap.objectClasses().length == 0) {
            throw new EruptWebApiRuntimeException(
                    "@EruptLdap(objectClasses = ...) must be declared on " + eruptModel.getEruptName() + " to write entries");
        }
        Attributes attrs = new BasicAttributes(true);
        BasicAttribute objectClass = new BasicAttribute("objectClass");
        for (String oc : ldap.objectClasses()) objectClass.add(oc);
        attrs.put(objectClass);
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            String name = fieldModel.getFieldName();
            Attribute attr = this.toAttribute(name, this.readValue(eruptModel, object, name));
            if (null != attr) attrs.put(attr);
        }
        return attrs;
    }

    private Attribute toAttribute(String name, Object value) {
        if (null == value) return null;
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) return null;
            BasicAttribute attr = new BasicAttribute(name);
            for (Object item : collection) if (null != item) attr.add(String.valueOf(item));
            return attr.size() == 0 ? null : attr;
        }
        if (value.getClass().isArray()) {
            return this.toAttribute(name, Arrays.asList((Object[]) value));
        }
        String s = String.valueOf(value);
        return s.isEmpty() ? null : new BasicAttribute(name, s);
    }

    private String buildFilter(EruptLdap ldap, List<Condition> conditions) {
        StringBuilder pushdown = new StringBuilder();
        for (Condition condition : conditions) {
            String key = this.escapeFilter(condition.getKey());
            Object value = condition.getValue();
            switch (condition.getExpression()) {
                case EQ -> pushdown.append('(').append(key).append('=').append(this.escapeFilter(String.valueOf(value))).append(')');
                case LIKE -> pushdown.append('(').append(key).append("=*").append(this.escapeFilter(String.valueOf(value))).append("*)");
                case NOT_NULL -> pushdown.append('(').append(key).append("=*)");
                case NULL -> pushdown.append("(!(").append(key).append("=*))");
                default -> { /* other operators are re-evaluated by the base class */ }
            }
        }
        if (pushdown.length() == 0) return ldap.filter();
        return "(&" + ldap.filter() + pushdown + ")";
    }

    private String escapeFilter(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '*' -> sb.append("\\2a");
                case '(' -> sb.append("\\28");
                case ')' -> sb.append("\\29");
                case '\\' -> sb.append("\\5c");
                case '\u0000' -> sb.append("\\00");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private String escapeRdn(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean edge = i == 0 || i == value.length() - 1;
            if (",+\"\\<>;".indexOf(c) >= 0 || (edge && (c == ' ' || c == '#'))) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private EruptWebApiRuntimeException wrap(NamingException e) {
        String detail = null == e.getMessage() ? e.getClass().getSimpleName() : e.getMessage();
        return new EruptWebApiRuntimeException(
                I18nTranslate.$translate("ldap.operation_failed") + " → " + detail);
    }

    private void close(DirContext ctx) {
        if (null == ctx) return;
        try {
            ctx.close();
        } catch (NamingException ignore) {
        }
    }

}
