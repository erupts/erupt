package xyz.erupt.designer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.designer.pojo.DesignerForm;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.linq.lambda.LambdaSee;

import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Convert designer json (mirroring erupt annotation structure) to entity source code.
 * <p>
 * Structure, imports, escaping and indentation are delegated to JavaPoet; this class only owns the
 * domain logic JavaPoet can't know: which annotation members to emit (only values that differ from
 * the member default, so the generated code stays minimal) and how each designer field maps to a
 * java type and ORM annotations.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Service
public class EruptCodeService {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    // member rendering order: well-known members first, sub-annotation configs last
    private static final List<String> MEMBER_ORDER = Arrays.asList("title", "name", "value", "label", "desc", "notNull", "type");

    // JavaPoet format codes
    private static final String S = "$S";
    private static final String L = "$L";
    private static final String TL = "$T.$L";

    public String generate(DesignerForm form) {
        if (null == form.getClassName() || !IDENTIFIER.matcher(form.getClassName()).matches()) {
            throw new EruptWebApiRuntimeException("Invalid class name: " + form.getClassName());
        }
        String eruptName = LambdaSee.method(Erupt::name);
        JsonObject eruptJson = Optional.ofNullable(form.getErupt()).orElse(new JsonObject());
        if (!eruptJson.has(eruptName)) eruptJson.addProperty(eruptName, form.getClassName());
        String tableName = Optional.ofNullable(form.getTableName())
                .filter(it -> !it.isEmpty()).orElse(camelToUnderline(form.getClassName()));
        TypeSpec.Builder type = TypeSpec.classBuilder(form.getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec(Erupt.class, eruptJson))
                .addAnnotation(AnnotationSpec.builder(Table.class).addMember(LambdaSee.method(Table::name), S, tableName).build())
                .addAnnotation(Entity.class)
                .addAnnotation(Getter.class)
                .addAnnotation(Setter.class);
        type.superclass(ClassName.get(BaseModel.class));
        for (DesignerForm.DesignerField field : Optional.ofNullable(form.getFields()).orElse(new ArrayList<>())) {
            type.addField(fieldSpec(form, field));
        }
        return JavaFile.builder(Optional.ofNullable(form.getPkg()).orElse(""), type.build())
                .indent("    ").skipJavaLangImports(true).build().toString();
    }

    private FieldSpec fieldSpec(DesignerForm form, DesignerForm.DesignerField field) {
        if (null == field.getFieldName() || !IDENTIFIER.matcher(field.getFieldName()).matches()) {
            throw new EruptWebApiRuntimeException("Invalid field name: " + field.getFieldName());
        }
        String editTypeMember = LambdaSee.method(Edit::type);
        EditType editType = Optional.ofNullable(field.getEdit())
                .filter(it -> it.has(editTypeMember))
                .map(it -> EditType.valueOf(it.get(editTypeMember).getAsString())).orElse(EditType.INPUT);
        AnnotationSpec.Builder eruptField = AnnotationSpec.builder(EruptField.class);
        if (null != field.getView())
            eruptField.addMember(LambdaSee.method(EruptField::views), L, annotationSpec(View.class, field.getView()));
        if (null != field.getEdit())
            eruptField.addMember(LambdaSee.method(EruptField::edit), L, annotationSpec(Edit.class, field.getEdit()));
        FieldSpec.Builder fieldSpec = FieldSpec.builder(fieldType(field, editType), field.getFieldName(), Modifier.PRIVATE)
                .addAnnotation(eruptField.build());
        for (AnnotationSpec orm : ormAnnotations(form, field, editType)) {
            fieldSpec.addAnnotation(orm);
        }
        return fieldSpec.build();
    }

    private TypeName fieldType(DesignerForm.DesignerField field, EditType editType) {
        if (null != field.getFieldType() && !field.getFieldType().isEmpty()) {
            return BigDecimal.class.getSimpleName().equals(field.getFieldType())
                    ? ClassName.get(BigDecimal.class) : ClassName.bestGuess(field.getFieldType());
        }
        return switch (editType) {
            case NUMBER, SLIDER, RATE -> ClassName.get(Integer.class);
            case BOOLEAN -> ClassName.get(Boolean.class);
            case DATE -> ClassName.get(Date.class);
            case REFERENCE_TREE, REFERENCE_TABLE, COMBINE -> linkErupt(field);
            case CHECKBOX, TAB_TREE, TAB_TABLE_REFER, TAB_TABLE_ADD ->
                    ParameterizedTypeName.get(ClassName.get(Set.class), linkErupt(field));
            default -> ClassName.get(String.class);
        };
    }

    private List<AnnotationSpec> ormAnnotations(DesignerForm form, DesignerForm.DesignerField field, EditType editType) {
        return switch (editType) {
            case REFERENCE_TREE, REFERENCE_TABLE ->
                    Arrays.asList(annotation(ManyToOne.class), joinColumn(field.getFieldName()));
            case COMBINE -> Arrays.asList(AnnotationSpec.builder(OneToOne.class)
                            .addMember(LambdaSee.method(OneToOne::cascade), TL, CascadeType.class, CascadeType.ALL.name()).build(),
                    joinColumn(field.getFieldName()));
            case CHECKBOX, TAB_TREE, TAB_TABLE_REFER -> List.of(annotation(ManyToMany.class));
            case TAB_TABLE_ADD -> Arrays.asList(AnnotationSpec.builder(OneToMany.class)
                            .addMember(LambdaSee.method(OneToMany::cascade), TL, CascadeType.class, CascadeType.ALL.name())
                            .addMember(LambdaSee.method(OneToMany::orphanRemoval), L, true).build(),
                    joinColumn(form.getClassName()));
            case HTML_EDITOR, CODE_EDITOR, MARKDOWN -> List.of(annotation(Lob.class));
            case DIVIDE, EMPTY, GROUP -> List.of(annotation(Transient.class));
            default -> List.of();
        };
    }

    private static AnnotationSpec annotation(Class<? extends Annotation> type) {
        return AnnotationSpec.builder(type).build();
    }

    private static AnnotationSpec joinColumn(String name) {
        return AnnotationSpec.builder(JoinColumn.class)
                .addMember(LambdaSee.method(JoinColumn::name), S, camelToUnderline(name) + "_id").build();
    }

    private TypeName linkErupt(DesignerForm.DesignerField field) {
        if (null == field.getLinkErupt() || !IDENTIFIER.matcher(field.getLinkErupt()).matches()) {
            throw new EruptWebApiRuntimeException("Field '" + field.getFieldName() + "' requires a linked erupt class");
        }
        return ClassName.get("", field.getLinkErupt());
    }

    // Build an annotation generically: emit only members whose json value differs from the member default
    private AnnotationSpec annotationSpec(Class<? extends Annotation> type, JsonObject json) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(type);
        Method[] methods = type.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparingInt(this::memberOrder).thenComparing(Method::getName));
        for (Method method : methods) {
            JsonElement je = json.get(method.getName());
            if (null == je || je.isJsonNull()) continue;
            CodeBlock value = memberValue(method.getReturnType(), je, method.getDefaultValue());
            if (null != value) builder.addMember(method.getName(), value);
        }
        return builder.build();
    }

    // returns null when the value equals the member default (no need to emit)
    @SuppressWarnings("unchecked")
    private CodeBlock memberValue(Class<?> rt, JsonElement je, Object def) {
        if (Class.class.isAssignableFrom(rt)) return null;
        if (rt.isArray()) {
            if (!je.isJsonArray()) return null;
            JsonArray arr = je.getAsJsonArray();
            if (arr.isEmpty()) return null;
            List<CodeBlock> items = new ArrayList<>();
            for (JsonElement item : arr) {
                items.add(this.memberValue(rt.getComponentType(), item, null));
            }
            return items.size() == 1 ? items.get(0) : CodeBlock.of("{$L}", CodeBlock.join(items, ", "));
        }
        if (rt.isAnnotation()) {
            if (!je.isJsonObject()) return null;
            Class<? extends Annotation> at = (Class<? extends Annotation>) rt;
            if (def instanceof Annotation && this.sameAsDefault(at, je.getAsJsonObject(), (Annotation) def))
                return null;
            return CodeBlock.of(L, this.annotationSpec(at, je.getAsJsonObject()));
        }
        if (rt.isEnum()) {
            String name = je.getAsString();
            if (def instanceof Enum && ((Enum<?>) def).name().equals(name)) return null;
            return CodeBlock.of(TL, rt, name);
        }
        if (rt == String.class) {
            String value = je.getAsString();
            if (value.equals(def)) return null;
            return CodeBlock.of(S, value);
        }
        if (rt == boolean.class) {
            boolean value = je.getAsBoolean();
            if (def instanceof Boolean && (Boolean) def == value) return null;
            return CodeBlock.of(L, value);
        }
        if (rt == float.class || rt == double.class) {
            double value = je.getAsDouble();
            if (def instanceof Number && ((Number) def).doubleValue() == value) return null;
            String str = value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
            return CodeBlock.of(L, rt == float.class ? str + "f" : str);
        }
        // int / long
        long value = je.getAsLong();
        if (def instanceof Number && ((Number) def).longValue() == value) return null;
        return CodeBlock.of(L, (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) ? value + "L" : String.valueOf(value));
    }

    // whether the json effective values equal the default annotation instance held by the parent member
    private boolean sameAsDefault(Class<? extends Annotation> type, JsonObject json, Annotation def) {
        try {
            for (Method method : type.getDeclaredMethods()) {
                if (Class.class.isAssignableFrom(method.getReturnType())) continue;
                JsonElement je = json.get(method.getName());
                Object defValue = method.invoke(def);
                if (null == je || je.isJsonNull()) {
                    if (null != method.getDefaultValue() && !deepEquals(method.getDefaultValue(), defValue))
                        return false;
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
