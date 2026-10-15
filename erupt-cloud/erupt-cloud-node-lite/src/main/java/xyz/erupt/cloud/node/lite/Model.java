package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptTag;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.RowOperation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An {@code @Erupt} class as the node serves it: its build JSON, produced from the annotations the way erupt's
 * proxies and {@code AnnotationProcess} produce it, and reflective access to its {@code @EruptField} members,
 * which are also a repository's columns.
 */
public final class Model<T> implements AnnotationJson.Resolver {

    // the one erupt i18n key that is an annotation default; titles are the developer's own text
    private static final Map<String, String> I18N = Map.of("erupt.operation.call_hint", "Confirm whether to perform this operation");

    final Registry registry;
    final Class<T> type;
    final Erupt erupt;
    final String name;
    final Map<String, FieldDef> fields = new LinkedHashMap<>();
    private final FieldDef pk;
    final List<Sort> orderBy = new ArrayList<>();

    Model(Class<T> type, Registry registry) {
        this.registry = registry;
        this.type = type;
        this.erupt = type.getAnnotation(Erupt.class);
        if (erupt == null) throw new IllegalArgumentException(type.getName() + " is not annotated with @Erupt");
        this.name = type.getSimpleName();
        List<FieldDef> declared = new ArrayList<>();
        for (Field f : members(type)) {
            if (f.isAnnotationPresent(EruptField.class)) declared.add(new FieldDef(this, f, f.getName().equals(erupt.primaryKeyCol())));
        }
        declared.sort(Comparator.comparingInt(f -> f.eruptField.sort()));
        for (FieldDef f : declared) {
            if (fields.putIfAbsent(f.name, f) != null) throw new IllegalArgumentException(name + ": field '" + f.name + "' declared twice");
        }
        this.pk = fields.get(erupt.primaryKeyCol());
        for (String part : erupt.orderBy().split(",")) {
            String[] words = part.strip().split("\\s+");
            if (words[0].isEmpty()) continue;
            boolean illegal = words.length > 2 || !fields.containsKey(words[0])
                              || words.length == 2 && !List.of("asc", "desc").contains(words[1].toLowerCase(Locale.ROOT));
            if (illegal) throw new IllegalArgumentException(name + ": orderBy '" + part.strip() + "' must be 'field [asc|desc]', by field name");
            orderBy.add(new Sort(words[0], words.length == 2 && words[1].equalsIgnoreCase("desc") ? Direction.DESC : Direction.ASC));
        }
        try {
            type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(name + " needs a no-argument constructor");
        }
    }

    /** {@code type} on its own, handlers made by their no-argument constructors; a node builds its models itself. */
    public static <T> Model<T> of(Class<T> type) {
        return new Model<>(type, Registry.standalone());
    }

    /** The name the server addresses: the simple class name. */
    public String name() {
        return name;
    }

    /** {@code @Erupt(name)}: what the menu shows. */
    public String title() {
        return erupt.name();
    }

    public Class<T> type() {
        return type;
    }

    /** The primary key column ({@code @Erupt(primaryKeyCol)}); null for a model that only serves as an operation form. */
    public String pk() {
        return pk == null ? null : pk.name;
    }

    /** The {@code @EruptField} members, which are the columns, in display order. */
    public List<String> columns() {
        return List.copyOf(fields.keySet());
    }

    /** The stored value of a column: a reference as the referenced record's primary key. */
    public Object get(T record, String column) {
        return field(column).column(record);
    }

    /** Sets a column from a stored value, converting it to the member's type (a driver's value, or a reference key). */
    public void set(T record, String column, Object value) {
        field(column).column(record, value);
    }

    public T newInstance() {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + type.getName(), e);
        }
    }

    FieldDef field(String column) {
        FieldDef f = fields.get(column);
        if (f == null) throw new IllegalArgumentException(name + " has no column '" + column + "'");
        return f;
    }

    FieldDef pkField() {
        if (pk == null) throw new IllegalArgumentException(name + " has no primary key field '" + erupt.primaryKeyCol() + "'");
        return pk;
    }

    // ---------------------------------------------------------------- records

    Map<String, Object> toRow(T record) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (FieldDef f : fields.values()) row.put(f.name, f.toJson(record));
        return row;
    }

    /** Writes the form fields of {@code body} into {@code record}; a missing or illegal value fails with the field's title. */
    T fill(T record, Map<?, ?> body) {
        for (FieldDef f : fields.values()) {
            if (!f.writable()) continue;
            Object value;
            try {
                value = f.fromJson(body.get(f.name));
            } catch (IllegalArgumentException e) {
                throw new EruptException(f.title() + ": " + e.getMessage());
            }
            if (value == null && f.edit.notNull()) throw new EruptException(f.title() + " cannot be empty");
            f.set(record, value);
        }
        return record;
    }

    @SuppressWarnings("unchecked")
    void proxies(Consumer<DataProxy<T>> hook) {
        for (Class<? extends DataProxy<?>> c : erupt.dataProxy()) hook.accept((DataProxy<T>) registry.instance(c));
    }

    // ---------------------------------------------------------------- build JSON

    /** The declared power, after {@code @Power(powerHandler)}. */
    PowerObject power() {
        PowerObject power = new PowerObject(erupt.power());
        if (!erupt.power().powerHandler().isInterface()) ((PowerHandler) registry.instance(erupt.power().powerHandler())).handler(power);
        return power;
    }

    /** The row operations visible to the acting user ({@code show}), keyed by their resolved code. */
    Map<String, RowOperation> operations() {
        Map<String, RowOperation> ops = new LinkedHashMap<>();
        for (RowOperation op : erupt.rowOperation()) {
            if (expr(op.show())) ops.put(op.code().isEmpty() ? Integer.toString(op.title().hashCode()) : op.code(), op);
        }
        return ops;
    }

    Map<String, Object> buildErupt() {
        Map<String, Object> json = AnnotationJson.toJson(erupt, this);
        Map<String, Object> extra = new LinkedHashMap<>();
        for (Class<? extends Annotation> ex : erupt.extra()) {
            Annotation a = type.getAnnotation(ex);
            if (a != null) extra.put(ex.getSimpleName(), AnnotationJson.toJson(a, this));
        }
        json.put("extra", extra);
        Map<String, Object> tags = new LinkedHashMap<>();
        for (Annotation a : type.getDeclaredAnnotations()) {
            if (a.annotationType().isAnnotationPresent(EruptTag.class)) tags.put(a.annotationType().getSimpleName(), AnnotationJson.toJson(a, this));
        }
        Map<String, Object> searchCondition = new LinkedHashMap<>();
        proxies(p -> p.searchCondition(searchCondition));
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("eruptName", name);
        model.put("eruptJson", json);
        model.put("eruptFieldModels", fields.values().stream().map(FieldDef::build).toList());
        model.put("searchCondition", searchCondition);
        model.put("extraRow", false);
        model.put("tags", tags);
        return model;
    }

    Map<String, Object> buildModel(Map<String, Object> power) {
        Map<String, Object> forms = new LinkedHashMap<>();
        operations().forEach((code, op) -> {
            if (op.eruptClass() != void.class && op.type() == RowOperation.Type.ERUPT) forms.put(code, registry.model(op.eruptClass()).buildErupt());
        });
        Map<String, Object> build = new LinkedHashMap<>();
        build.put("eruptModel", buildErupt());
        build.put("power", power);
        build.put("tabErupts", null);
        build.put("combineErupts", null);
        build.put("operationErupts", forms.isEmpty() ? null : forms);
        return build;
    }

    /** What erupt's EruptProxy / RowOperationProxy answer: entries hidden by {@code show} left out, codes and hints resolved. */
    @Override
    public Object value(Annotation a, Method m) {
        String attr = m.getName();
        if (a instanceof Erupt e) {
            switch (attr) {
                case "rowOperation":
                    return operations().values().toArray(RowOperation[]::new);
                case "drills":
                    return Arrays.stream(e.drills()).filter(d -> expr(d.show())).toArray(Drill[]::new);
                case "vis":
                    return Arrays.stream(e.vis()).filter(v -> expr(v.show())).toArray(Vis[]::new);
                default:
                    break;
            }
        }
        if (a instanceof RowOperation op) {
            if (attr.equals("code") && op.code().isEmpty()) return Integer.toString(op.title().hashCode());
            if (attr.equals("callHint")) return I18N.getOrDefault(op.callHint(), op.callHint());
        }
        return AnnotationJson.raw(a, m);
    }

    boolean expr(ExprBool expr) {
        if (expr.exprHandler().isInterface()) return expr.value();
        return ((ExprBool.ExprHandler) registry.instance(expr.exprHandler())).handler(expr.value(), expr.params());
    }

    /** Instance fields in declaration order, inherited ones first (erupt's ReflectUtil.findClassAllFields). */
    private static List<Field> members(Class<?> type) {
        Deque<Class<?>> chain = new ArrayDeque<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) chain.push(c);
        List<Field> out = new ArrayList<>();
        for (Class<?> c : chain) {
            for (Field f : c.getDeclaredFields()) if (!Modifier.isStatic(f.getModifiers())) out.add(f);
        }
        return out;
    }

    /** Where a model finds handler instances and the models it references. */
    interface Registry {

        Object instance(Class<?> type);

        Model<?> model(Class<?> type);

        static Registry standalone() {
            Map<Class<?>, Object> instances = new java.util.concurrent.ConcurrentHashMap<>();
            Map<Class<?>, Model<?>> models = new java.util.concurrent.ConcurrentHashMap<>();
            return new Registry() {
                @Override
                public Object instance(Class<?> type) {
                    return instances.computeIfAbsent(type, Registry::construct);
                }

                @Override
                public Model<?> model(Class<?> type) {
                    Model<?> m = models.get(type);
                    if (m == null) models.put(type, m = new Model<>(type, this));
                    return m;
                }
            };
        }

        static Object construct(Class<?> type) {
            try {
                var constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("cannot create " + type.getName() + ": give it a no-argument constructor or set Node.factory", e);
            }
        }
    }
}
