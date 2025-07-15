package xyz.erupt.ai.util;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.util.ReflectUtil;

import javax.lang.model.element.Modifier;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2025/7/14 22:13
 */
public class ClassTemplateUtil {

    public static String geneEruptFormClass(Class<?> erupt) {
        // 构建类
        TypeSpec.Builder builder = TypeSpec.classBuilder(erupt.getSimpleName()).addModifiers(Modifier.PUBLIC);
        ReflectUtil.findClassAllFields(erupt, field -> {
            Optional.ofNullable(field.getDeclaredAnnotation(EruptField.class)).ifPresent(eruptField -> {
                if (!"".equals(eruptField.edit().title())) {
                    FieldSpec.Builder fieldSpec = FieldSpec.builder(field.getType(), field.getName(), Modifier.PUBLIC);
                    fieldSpec.addJavadoc(eruptField.edit().title());
                    builder.addField(fieldSpec.build());
                }
            });
        });
        return builder.build().toString();
    }

}
