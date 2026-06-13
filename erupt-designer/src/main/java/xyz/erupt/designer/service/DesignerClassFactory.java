package xyz.erupt.designer.service;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.designer.model.DesignerForm;
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

    // 引入 erupt-flow 模块时此注解可加载，设计器模型即自动具备审批流能力（注解直接烙进字节码，供 flow 反射识别）
    private static final Class<? extends Annotation> ERUPT_FLOW = loadOptionalAnnotation("xyz.erupt.flow.annotation.EruptFlow");

    // 设计器伪装类位于独立运行时包，以此区分真实 @Erupt 类
    public static boolean designerClass(Class<?> clazz) {
        return clazz.getName().startsWith(PKG);
    }

    public static Class<?> build(DesignerForm form) {
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(BaseModel.class)
                .name(PKG + form.getClassName())
                .annotateType(AnnotationDescription.Builder.ofType(EruptDataProcessor.class)
                        .define("value", DATA_PROCESSOR).build());
        // 未引入 erupt-flow 时 ERUPT_FLOW 为 null，跳过即可，不产生硬依赖
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
        switch (editType) {
            case NUMBER:
            case SLIDER:
            case RATE:
                switch (Optional.ofNullable(field.getFieldType()).orElse("Integer")) {
                    case "Long":
                        return Long.class;
                    case "Double":
                        return Double.class;
                    case "Float":
                        return Float.class;
                    case "BigDecimal":
                        return BigDecimal.class;
                    default:
                        return Integer.class;
                }
            case BOOLEAN:
                return Boolean.class;
            case DATE:
                return Date.class;
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
            case COMBINE:
                // 引用数据以 {id, label...} 结构存储，无 ORM 实体
                return Map.class;
            case CHECKBOX:
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
                return java.util.List.class;
            default:
                return String.class;
        }
    }

}
