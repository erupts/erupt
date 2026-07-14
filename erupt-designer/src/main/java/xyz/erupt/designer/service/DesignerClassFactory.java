package xyz.erupt.designer.service;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.designer.pojo.DesignerForm;
import xyz.erupt.jpa.model.BaseModel;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Generate a real erupt class at runtime: plain fields with proper java types so that the
 * standard pipeline (Gson conversion, reflection, validation) works exactly as it does for
 * hand-written entities. Annotations stay disguised on the EruptModel via JsonAnnotationProxy —
 * the bytecode only carries fields plus the @EruptDataProcessor routing annotation.
 * <p>
 * Each build loads into a fresh classloader (WRAPPER strategy), so re-publishing the same
 * class name simply produces a new class and the old one becomes garbage.
 *
 * @author YuePeng
 * date 2026-06-12
 */
public class DesignerClassFactory {

    public static final String DATA_PROCESSOR = "erupt-designer-data";

    private static final String PKG = "xyz.erupt.designer.runtime.";

    // when erupt-flow is on the classpath this annotation loads; designer models then gain approval-flow capability (annotation baked into bytecode so flow can detect it via reflection)
    private static final Class<? extends Annotation> ERUPT_FLOW = loadOptionalAnnotation("xyz.erupt.flow.annotation.EruptFlow");

    // designer disguise classes live in a dedicated runtime package to distinguish them from real @Erupt classes
    public static boolean designerClass(Class<?> clazz) {
        return clazz.getName().startsWith(PKG);
    }

    public static Class<?> build(DesignerForm form) {
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(BaseModel.class)
                .name(PKG + form.getClassName())
                .annotateType(AnnotationDescription.Builder.ofType(EruptDataProcessor.class)
                        .define("value", DATA_PROCESSOR).build());
        // ERUPT_FLOW is null when erupt-flow is absent; skip without creating a hard dependency
        if (null != ERUPT_FLOW) {
            builder = builder.annotateType(AnnotationDescription.Builder.ofType(ERUPT_FLOW).build());
        }
        for (DesignerForm.DesignerField field : form.getFields()) {
            builder = builder.defineField(field.getFieldName(), javaType(field), Visibility.PRIVATE);
        }
        return builder.make().load(DesignerClassFactory.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> loadOptionalAnnotation(String className) {
        try {
            return (Class<? extends Annotation>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Class<?> javaType(DesignerForm.DesignerField field) {
        EditType editType = Optional.ofNullable(field.getEdit())
                .filter(it -> it.has("type"))
                .map(it -> EditType.valueOf(it.get("type").getAsString())).orElse(EditType.INPUT);
        return switch (editType) {
            case NUMBER, SLIDER, RATE -> switch (Optional.ofNullable(field.getFieldType()).orElse("Integer")) {
                case "Long" -> Long.class;
                case "Double" -> Double.class;
                case "Float" -> Float.class;
                case "BigDecimal" -> BigDecimal.class;
                default -> Integer.class;
            };
            case BOOLEAN -> Boolean.class;
            case DATE -> Date.class;
            case REFERENCE_TREE, REFERENCE_TABLE, COMBINE ->
                // reference data is stored as {id, label...}; no ORM entity involved
                    Map.class;
            case MULTI_CHOICE -> java.util.Set.class;
            case CHECKBOX, TAB_TREE, TAB_TABLE_ADD, TAB_TABLE_REFER -> java.util.List.class;
            default -> String.class;
        };
    }

}
