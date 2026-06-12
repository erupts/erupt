package xyz.erupt.designer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.designer.model.DesignerForm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Convert designer json (mirroring erupt annotation structure) to entity source code.
 * <p>
 * Annotation members are rendered generically by reflection: only values that differ from
 * the annotation member defaults are emitted, so the generated code stays minimal.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Service
public class EruptCodeService {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    // member rendering order: well-known members first, sub-annotation configs last
    private static final List<String> MEMBER_ORDER = Arrays.asList("title", "name", "value", "label", "desc", "notNull", "type");

    public String generate(DesignerForm form) {
        if (null == form.getClassName() || !IDENTIFIER.matcher(form.getClassName()).matches()) {
            throw new EruptWebApiRuntimeException("Invalid class name: " + form.getClassName());
        }
        Set<String> imports = new LinkedHashSet<>(Arrays.asList(
                "xyz.erupt.annotation.*",
                "xyz.erupt.annotation.sub_erupt.*",
                "xyz.erupt.annotation.sub_field.*",
                "xyz.erupt.annotation.sub_field.sub_edit.*",
                "jakarta.persistence.*",
                "lombok.Getter",
                "lombok.Setter"
        ));
        StringBuilder body = new StringBuilder();
        for (DesignerForm.DesignerField field : Optional.ofNullable(form.getFields()).orElse(new ArrayList<>())) {
            body.append(this.fieldCode(form, field, imports));
        }
        StringBuilder code = new StringBuilder();
        if (null != form.getPkg() && !form.getPkg().isEmpty()) {
            code.append("package ").append(form.getPkg()).append(";\n\n");
        }
        String extendsModel = Optional.ofNullable(form.getExtendsModel()).orElse("");
        if (!extendsModel.isEmpty()) imports.add("xyz.erupt.jpa.model." + extendsModel);
        imports.forEach(it -> code.append("import ").append(it).append(";\n"));
        code.append("\n");
        JsonObject eruptJson = Optional.ofNullable(form.getErupt()).orElse(new JsonObject());
        if (!eruptJson.has("name")) eruptJson.addProperty("name", form.getClassName());
        code.append(annotationCode(Erupt.class, eruptJson, 0)).append("\n");
        code.append("@Table(name = \"").append(Optional.ofNullable(form.getTableName())
                .filter(it -> !it.isEmpty()).orElse(camelToUnderline(form.getClassName()))).append("\")\n");
        code.append("@Entity\n@Getter\n@Setter\n");
        code.append("public class ").append(form.getClassName());
        if (!extendsModel.isEmpty()) code.append(" extends ").append(extendsModel);
        code.append(" {\n").append(body).append("\n}");
        return code.toString();
    }

    private String fieldCode(DesignerForm form, DesignerForm.DesignerField field, Set<String> imports) {
        if (null == field.getFieldName() || !IDENTIFIER.matcher(field.getFieldName()).matches()) {
            throw new EruptWebApiRuntimeException("Invalid field name: " + field.getFieldName());
        }
        EditType editType = Optional.ofNullable(field.getEdit())
                .filter(it -> it.has("type"))
                .map(it -> EditType.valueOf(it.get("type").getAsString())).orElse(EditType.INPUT);
        StringBuilder sb = new StringBuilder("\n    @EruptField(\n");
        List<String> parts = new ArrayList<>();
        if (null != field.getView()) parts.add("            views = " + annotationCode(View.class, field.getView(), 3));
        if (null != field.getEdit()) parts.add("            edit = " + annotationCode(Edit.class, field.getEdit(), 3));
        sb.append(String.join(",\n", parts)).append("\n    )\n");
        for (String orm : this.ormAnnotations(form, field, editType)) {
            sb.append("    ").append(orm).append("\n");
        }
        sb.append("    private ").append(this.javaType(field, editType, imports)).append(" ").append(field.getFieldName()).append(";\n");
        return sb.toString();
    }

    private String javaType(DesignerForm.DesignerField field, EditType editType, Set<String> imports) {
        if (null != field.getFieldType() && !field.getFieldType().isEmpty()) {
            if ("BigDecimal".equals(field.getFieldType())) imports.add("java.math.BigDecimal");
            return field.getFieldType();
        }
        switch (editType) {
            case NUMBER:
            case SLIDER:
            case RATE:
                return "Integer";
            case BOOLEAN:
                return "Boolean";
            case DATE:
                imports.add("java.util.Date");
                return "Date";
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
            case COMBINE:
                return this.linkErupt(field);
            case CHECKBOX:
            case TAB_TREE:
            case TAB_TABLE_REFER:
            case TAB_TABLE_ADD:
                imports.add("java.util.Set");
                return "Set<" + this.linkErupt(field) + ">";
            default:
                return "String";
        }
    }

    private List<String> ormAnnotations(DesignerForm form, DesignerForm.DesignerField field, EditType editType) {
        switch (editType) {
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
                return Arrays.asList("@ManyToOne", "@JoinColumn(name = \"" + camelToUnderline(field.getFieldName()) + "_id\")");
            case COMBINE:
                return Arrays.asList("@OneToOne(cascade = CascadeType.ALL)", "@JoinColumn(name = \"" + camelToUnderline(field.getFieldName()) + "_id\")");
            case CHECKBOX:
            case TAB_TREE:
            case TAB_TABLE_REFER:
                return List.of("@ManyToMany");
            case TAB_TABLE_ADD:
                return Arrays.asList("@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)",
                        "@JoinColumn(name = \"" + camelToUnderline(form.getClassName()) + "_id\")");
            case HTML_EDITOR:
            case CODE_EDITOR:
            case MARKDOWN:
                return List.of("@Lob");
            case DIVIDE:
            case EMPTY:
            case GROUP:
                return List.of("@Transient");
            default:
                return List.of();
        }
    }

    private String linkErupt(DesignerForm.DesignerField field) {
        if (null == field.getLinkErupt() || !IDENTIFIER.matcher(field.getLinkErupt()).matches()) {
            throw new EruptWebApiRuntimeException("Field '" + field.getFieldName() + "' requires a linked erupt class");
        }
        return field.getLinkErupt();
    }

    // Render an annotation generically: emit only members whose json value differs from the member default
    public String annotationCode(Class<? extends Annotation> type, JsonObject json, int indentLevel) {
        List<String> parts = new ArrayList<>();
        Method[] methods = type.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparingInt(this::memberOrder).thenComparing(Method::getName));
        for (Method method : methods) {
            JsonElement je = json.get(method.getName());
            if (null == je || je.isJsonNull()) continue;
            String value = this.memberValue(method.getReturnType(), je, method.getDefaultValue(), indentLevel);
            if (null != value) parts.add(method.getName() + " = " + value);
        }
        if (parts.isEmpty()) return "@" + type.getSimpleName();
        String joined = String.join(", ", parts);
        if (joined.length() > 90) {
            String indent = "    ".repeat(indentLevel + 1);
            joined = "\n" + indent + String.join(",\n" + indent, parts) + "\n" + "    ".repeat(indentLevel);
        }
        return "@" + type.getSimpleName() + "(" + joined + ")";
    }

    // returns null when the value equals the member default (no need to emit)
    @SuppressWarnings("unchecked")
    private String memberValue(Class<?> rt, JsonElement je, Object def, int indentLevel) {
        if (Class.class.isAssignableFrom(rt)) return null;
        if (rt.isArray()) {
            if (!je.isJsonArray()) return null;
            JsonArray arr = je.getAsJsonArray();
            if (arr.isEmpty()) return null;
            List<String> items = new ArrayList<>();
            for (JsonElement item : arr) {
                items.add(this.memberValue(rt.getComponentType(), item, null, indentLevel));
            }
            return items.size() == 1 ? items.get(0) : "{" + String.join(", ", items) + "}";
        }
        if (rt.isAnnotation()) {
            if (!je.isJsonObject()) return null;
            Class<? extends Annotation> at = (Class<? extends Annotation>) rt;
            if (def instanceof Annotation && this.sameAsDefault(at, je.getAsJsonObject(), (Annotation) def)) return null;
            return this.annotationCode(at, je.getAsJsonObject(), indentLevel + 1);
        }
        if (rt.isEnum()) {
            String name = je.getAsString();
            if (def instanceof Enum && ((Enum<?>) def).name().equals(name)) return null;
            return this.enumRef(rt) + "." + name;
        }
        if (rt == String.class) {
            String value = je.getAsString();
            if (value.equals(def)) return null;
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (rt == boolean.class) {
            boolean value = je.getAsBoolean();
            if (def instanceof Boolean && (Boolean) def == value) return null;
            return String.valueOf(value);
        }
        if (rt == float.class || rt == double.class) {
            double value = je.getAsDouble();
            if (def instanceof Number && ((Number) def).doubleValue() == value) return null;
            String str = value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
            return rt == float.class ? str + "f" : str;
        }
        // int / long
        long value = je.getAsLong();
        if (def instanceof Number && ((Number) def).longValue() == value) return null;
        return (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) ? value + "L" : String.valueOf(value);
    }

    // whether the json effective values equal the default annotation instance held by the parent member
    private boolean sameAsDefault(Class<? extends Annotation> type, JsonObject json, Annotation def) {
        try {
            for (Method method : type.getDeclaredMethods()) {
                if (Class.class.isAssignableFrom(method.getReturnType())) continue;
                JsonElement je = json.get(method.getName());
                Object defValue = method.invoke(def);
                if (null == je || je.isJsonNull()) {
                    if (null != method.getDefaultValue() && !deepEquals(method.getDefaultValue(), defValue)) return false;
                    continue;
                }
                Class<?> rt = method.getReturnType();
                if (rt.isArray()) {
                    if (je.getAsJsonArray().size() != Array.getLength(defValue)) return false;
                    if (!je.getAsJsonArray().isEmpty()) return false; // non-empty arrays → always emit
                } else if (rt.isAnnotation()) {
                    //noinspection unchecked
                    if (!je.isJsonObject() || !sameAsDefault((Class<? extends Annotation>) rt, je.getAsJsonObject(), (Annotation) defValue))
                        return false;
                } else if (rt.isEnum()) {
                    if (!((Enum<?>) defValue).name().equals(je.getAsString())) return false;
                } else if (rt == String.class) {
                    if (!defValue.equals(je.getAsString())) return false;
                } else if (rt == boolean.class) {
                    if ((Boolean) defValue != je.getAsBoolean()) return false;
                } else if (rt == float.class || rt == double.class) {
                    if (((Number) defValue).doubleValue() != je.getAsDouble()) return false;
                } else {
                    if (((Number) defValue).longValue() != je.getAsLong()) return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean deepEquals(Object a, Object b) {
        if (a != null && a.getClass().isArray()) return Array.getLength(a) == Array.getLength(b);
        return java.util.Objects.equals(a, b);
    }

    // nested enum reference, e.g. DateType.Type / EditType
    private String enumRef(Class<?> enumType) {
        StringBuilder name = new StringBuilder(enumType.getSimpleName());
        Class<?> enclosing = enumType.getEnclosingClass();
        while (null != enclosing) {
            name.insert(0, enclosing.getSimpleName() + ".");
            enclosing = enclosing.getEnclosingClass();
        }
        return name.toString();
    }

    private int memberOrder(Method method) {
        int index = MEMBER_ORDER.indexOf(method.getName());
        if (index >= 0) return index;
        return method.getReturnType().isAnnotation() || method.getReturnType().isArray() ? 90 : 50;
    }

    public static String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
