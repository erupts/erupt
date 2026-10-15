package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.annotation.config.QueryExpression;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One {@code @EruptField} member: its JSON, built the way erupt's field proxies resolve it (AUTO edit, view, date,
 * bool and search types inferred from the member), and the conversion of browser values into it.
 */
final class FieldDef implements AnnotationJson.Resolver {

    // what erupt renders for a field whose @Edit(ifRender) is off: an edit without a title (EruptTpl.field)
    @EruptField
    private static Object blank;

    private static final Edit BLANK_EDIT;

    static {
        try {
            BLANK_EDIT = FieldDef.class.getDeclaredField("blank").getAnnotation(EruptField.class).edit();
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final Model<?> owner;
    final Field field;
    final String name;
    final EruptField eruptField;
    final Edit edit;
    /** The resolved edit type: never AUTO. */
    final EditType type;
    final boolean pk;
    FieldDef(Model<?> owner, Field field, boolean pk) {
        this.owner = owner;
        this.field = field;
        this.name = field.getName();
        this.eruptField = field.getAnnotation(EruptField.class);
        this.edit = eruptField.edit();
        this.pk = pk;
        this.type = edit.type() == EditType.AUTO ? inferType() : edit.type();
        field.setAccessible(true);
        Class<?> t = field.getType();
        if (Collection.class.isAssignableFrom(t) || Map.class.isAssignableFrom(t) || t.isArray()) {
            throw new IllegalArgumentException(owner.name + "." + name + ": " + t.getSimpleName() + " members are not supported yet");
        }
    }

    // ---------------------------------------------------------------- what the node reads from the declaration

    String title() {
        return edit.title();
    }

    /** Written from the form: erupt shows a field in the form when its edit has a title. */
    boolean writable() {
        return !pk && !edit.title().isEmpty() && edit.show();
    }

    boolean searchable() {
        return !pk && edit.search().value();
    }

    boolean sortable() {
        return pk || Arrays.stream(eruptField.views()).anyMatch(View::sortable);
    }

    boolean isReference() {
        return type == EditType.REFERENCE_TABLE || type == EditType.REFERENCE_TREE;
    }

    /** The model a reference points at. */
    Model<?> target() {
        return owner.registry.model(field.getType());
    }

    /** The key of the referenced record in the picked value the browser sends. */
    String targetKey() {
        return type == EditType.REFERENCE_TREE ? edit.referenceTreeType().id() : edit.referenceTableType().id();
    }

    boolean dateOnly() {
        return dateType() != DateType.Type.DATE_TIME;
    }

    // ---------------------------------------------------------------- JSON

    Map<String, Object> build() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("fieldName", name);
        json.put("fieldReturnName", Values.isNumber(field.getType()) ? "number" : field.getType().getSimpleName());
        json.put("eruptFieldJson", AnnotationJson.toJson(eruptField, this));
        json.put("componentValue", type == EditType.CHOICE ? choices() : type == EditType.TAGS ? List.of(edit.tagsType().tags()) : null);
        return json;
    }

    /** The options of a CHOICE: {@code vl}, then every fetch handler, else the constants of an enum member. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Map<String, Object>> choices() {
        List<VLModel> vls = new ArrayList<>();
        for (VL vl : edit.choiceType().vl()) vls.add(new VLModel(vl));
        for (Class<? extends ChoiceFetchHandler> handler : edit.choiceType().fetchHandler()) {
            if (handler.isInterface()) continue;
            List<VLModel> fetched = ((ChoiceFetchHandler<Object>) owner.registry.instance(handler)).fetch(edit.choiceType().fetchHandlerParams());
            if (fetched != null) vls.addAll(fetched);
        }
        if (vls.isEmpty() && field.getType().isEnum()) {
            for (Object c : field.getType().getEnumConstants()) vls.add(new VLModel(((Enum<?>) c).name(), ((Enum<?>) c).name()));
        }
        List<Map<String, Object>> options = new ArrayList<>();
        for (VLModel vl : vls) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("value", vl.getValue());
            o.put("label", vl.getLabel());
            o.put("desc", blankToNull(vl.getDesc()));
            o.put("color", blankToNull(vl.getColor()));
            o.put("disable", vl.isDisable());
            if (vl.getExtra() != null && !"".equals(vl.getExtra())) o.put("extra", vl.getExtra());
            options.add(o);
        }
        return options;
    }

    private static String blankToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    /** What erupt's proxies answer for an attribute of this field's annotations. */
    @Override
    public Object value(Annotation a, Method m) {
        Object raw = AnnotationJson.raw(a, m);
        String attr = m.getName();
        if (a instanceof EruptField f && attr.equals("views")) {
            return Arrays.stream(f.views()).filter(v -> expr(v.ifRender())).toArray(View[]::new);
        }
        // @Edit(ifRender) off renders an edit without a title, left unresolved as erupt does
        if (a instanceof EruptField && attr.equals("edit")) return expr(edit.ifRender()) ? raw : BLANK_EDIT;
        if (a instanceof Edit && attr.equals("type")) return a == edit ? type : raw;
        if (a instanceof DateType && attr.equals("type")) return dateType();
        if (a instanceof BoolType b && attr.equals("type") && b.type() == BoolType.Type.AUTO) {
            return edit.notNull() ? BoolType.Type.SWITCH : BoolType.Type.RADIO;
        }
        if (a instanceof Search s && attr.equals("operator") && s.operator() == QueryExpression.AUTO) {
            return type == EditType.DATE ? QueryExpression.RANGE : QueryExpression.EQ;
        }
        if (a instanceof View v && attr.equals("type") && v.type() == ViewType.AUTO) return viewType();
        if (a instanceof Readonly r && !r.exprHandler().isInterface() && (attr.equals("add") || attr.equals("edit"))) {
            Readonly.ReadonlyHandler handler = (Readonly.ReadonlyHandler) owner.registry.instance(r.exprHandler());
            return attr.equals("add") ? handler.add(r.add(), r.params()) : handler.edit(r.edit(), r.params());
        }
        return raw;
    }

    boolean expr(ExprBool expr) {
        if (expr.exprHandler().isInterface()) return expr.value();
        return ((ExprBool.ExprHandler) owner.registry.instance(expr.exprHandler())).handler(expr.value(), expr.params());
    }

    // EditProxy: AUTO from the member's Java type
    private EditType inferType() {
        Class<?> t = field.getType();
        if (t.isEnum()) return EditType.CHOICE;
        if (t.isAnnotationPresent(Erupt.class)) {
            return t.getAnnotation(Erupt.class).tree().pid().isEmpty() ? EditType.REFERENCE_TABLE : EditType.REFERENCE_TREE;
        }
        if (Map.class.isAssignableFrom(t)) return EditType.KEY_VALUE;
        if (t == boolean.class || t == Boolean.class) return EditType.BOOLEAN;
        if (Values.isNumber(t)) return EditType.NUMBER;
        if (Values.isDate(t)) return EditType.DATE;
        return EditType.INPUT;
    }

    // DateTypeProxy / EruptUtil.inferDateType: a java.util.Date is a timestamp unless JPA's @Temporal narrows it
    DateType.Type dateType() {
        DateType.Type declared = edit.dateType().type();
        if (!declared.name().equals("AUTO")) return declared; // AUTO: erupt-annotation 2.3.1+
        Class<?> t = field.getType();
        if (t == LocalDateTime.class) return DateType.Type.DATE_TIME;
        if (t == LocalTime.class) return DateType.Type.TIME;
        if (t == java.util.Date.class) {
            for (Annotation a : field.getAnnotations()) {
                if (!a.annotationType().getSimpleName().equals("Temporal")) continue;
                try {
                    String temporal = String.valueOf(a.annotationType().getMethod("value").invoke(a));
                    if (temporal.equals("DATE")) return DateType.Type.DATE;
                    if (temporal.equals("TIME")) return DateType.Type.TIME;
                } catch (ReflectiveOperationException ignored) {
                    // not JPA's @Temporal
                }
            }
            return DateType.Type.DATE_TIME;
        }
        return DateType.Type.DATE;
    }

    // ViewProxy: AUTO from the edit type, else from the member's Java type
    private ViewType viewType() {
        if (!edit.title().isEmpty()) {
            switch (type) {
                case ATTACHMENT:
                    return edit.attachmentType().type() == AttachmentType.Type.IMAGE ? ViewType.IMAGE : ViewType.ATTACHMENT;
                case CHOICE:
                    return ViewType.TEXT;
                case SIGNATURE:
                    return ViewType.IMAGE_BASE64;
                case DATE:
                    return dateType() == DateType.Type.DATE_TIME ? ViewType.DATE_TIME : ViewType.DATE;
                case HTML_EDITOR:
                    return ViewType.HTML;
                case CODE_EDITOR:
                    return ViewType.CODE;
                case MAP:
                    return ViewType.MAP;
                case COLOR:
                    return ViewType.COLOR;
                case ICON:
                    return ViewType.ICON;
                case KEY_VALUE:
                    return ViewType.KEY_VALUE;
                case TAB_TABLE_ADD, MULTI_FORM, TAB_TREE, TAB_TABLE_REFER, CHECKBOX, TRANSFER, MULTI_CHOICE:
                    return ViewType.TAB_VIEW;
                case MARKDOWN:
                    return ViewType.MARKDOWN;
                case PASSWORD:
                    return ViewType.PASSWORD;
                default:
                    break;
            }
        }
        Class<?> t = field.getType();
        if (t == boolean.class || t == Boolean.class) return ViewType.BOOLEAN;
        if (Values.isDate(t)) return dateType() == DateType.Type.DATE_TIME ? ViewType.DATE_TIME : ViewType.DATE;
        if (Values.isNumber(t)) return ViewType.NUMBER;
        return ViewType.TEXT;
    }

    // ---------------------------------------------------------------- values

    /** The member value of {@code record}. */
    Object get(Object record) {
        try {
            return field.get(record);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    void set(Object record, Object value) {
        if (value == null && field.getType().isPrimitive()) return;
        try {
            field.set(record, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /** What a repository stores: a reference as the referenced record's key, everything else as it is. */
    Object column(Object record) {
        Object value = get(record);
        return isReference() && value != null ? target().pkField().get(value) : value;
    }

    /** Sets the member from a stored value (a driver's type, or a key for a reference). */
    void column(Object record, Object stored) {
        set(record, fromColumn(stored));
    }

    Object fromColumn(Object stored) {
        if (stored == null) return null;
        if (!isReference()) return Values.convert(stored, field.getType());
        Model<?> target = target();
        Object ref = target.newInstance();
        target.pkField().column(ref, stored);
        return ref;
    }

    /** A value from the browser as the member value; throws IllegalArgumentException when it is illegal. */
    Object fromJson(Object value) {
        if (isReference()) {
            // the browser sends the picked record as {<id>: ..., <label>: ...}
            if (value instanceof Map<?, ?> picked) value = picked.get(targetKey());
            return value == null || "".equals(value) ? null : fromColumn(target().pkField().fromJson(value));
        }
        Object converted = Values.convert(value, field.getType());
        if (converted != null && type == EditType.CHOICE && choiceValidate() && !field.getType().isEnum()) {
            String text = Values.text(converted);
            if (choices().stream().noneMatch(o -> text.equals(o.get("value")))) throw new IllegalArgumentException("illegal option '" + text + "'");
        }
        return converted;
    }

    /** {@code @ChoiceType(validate)}, on unless switched off (the attribute exists from erupt-annotation 2.3.1). */
    private boolean choiceValidate() {
        try {
            return !Boolean.FALSE.equals(edit.choiceType().annotationType().getMethod("validate").invoke(edit.choiceType()));
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    /** A stored value as JSON. A reference travels as its key: the server resolves labels (PROTOCOL.md §7.3). */
    Object toJson(Object record) {
        return Values.toJson(column(record), type == EditType.DATE && dateOnly());
    }

    /** A search value as the stored value it compares with. */
    Object toCondition(Object value) {
        if (isReference()) {
            if (value instanceof Map<?, ?> picked) value = picked.get(targetKey());
            return target().pkField().fromJson(value);
        }
        return Values.convert(value, field.getType());
    }
}
