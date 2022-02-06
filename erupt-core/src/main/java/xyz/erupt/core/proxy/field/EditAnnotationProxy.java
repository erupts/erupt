package xyz.erupt.core.proxy.field;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.util.EruptUtil;

/**
 * @author YuePeng
 * date 2022/2/6 10:13
 */
public class EditAnnotationProxy extends AnnotationProxy<Edit> {

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        Object rtn = this.invoke(invocation);
        if ("type".equals(invocation.getMethod().getName())) {
            if (EditType.AUTO.name().equals(rtn.toString())) {
                //根据返回类型推断
                String returnType = this.field.getType().getSimpleName();
                if (boolean.class.getSimpleName().equalsIgnoreCase(returnType)) {
                    return EditType.BOOLEAN;
                } else if (JavaType.NUMBER.equals(returnType)) {
                    return EditType.NUMBER;
                } else if (EruptUtil.isDateField(returnType)) {
                    return EditType.DATE;
                } else {
                    return EditType.INPUT;
                }
                //根据属性名推断
//                if (ArrayUtils.contains(AnnotationUtil.getEditTypeMapping(EditType.TEXTAREA).nameInfer(), returnType)) {
//                    return EditType.TEXTAREA;
//                }
            }
        }
        return rtn;
    }

}
