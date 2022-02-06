package xyz.erupt.core.proxy.erupt_field;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.util.EruptUtil;

/**
 * @author YuePeng
 * date 2022/2/6 10:13
 */
public class ViewAnnotationProxy extends AnnotationProxy<View> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        Object rtn = this.invoke(invocation);
        if ("type".equals(invocation.getMethod().getName())) {
            if (ViewType.AUTO.name().equals(rtn.toString())) {
                if (!AnnotationConst.EMPTY_STR.equals(this.eruptField.edit().title())) {
                    switch (this.eruptField.edit().type()) {
                        case ATTACHMENT:
                            if (this.eruptField.edit().attachmentType().type() == AttachmentType.Type.IMAGE) {
                                return ViewType.IMAGE;
                            } else {
                                return ViewType.ATTACHMENT;
                            }
                        case CHOICE:
                            return ViewType.TEXT;
                        case DATE:
                            return ViewType.DATE;
                        case HTML_EDITOR:
                            return ViewType.HTML;
                        case CODE_EDITOR:
                            return ViewType.CODE;
                        case MAP:
                            return ViewType.MAP;
                        case TAB_TABLE_ADD:
                        case TAB_TREE:
                        case TAB_TABLE_REFER:
                        case CHECKBOX:
                            return ViewType.TAB_VIEW;
                    }
                }
                String returnType = this.field.getType().getSimpleName();
                if (boolean.class.getSimpleName().equalsIgnoreCase(returnType.toLowerCase())) {
                    return ViewType.BOOLEAN;
                } else if (EruptUtil.isDateField(returnType)) {
                    return ViewType.DATE;
                } else if (JavaType.NUMBER.equals(returnType)) {
                    return ViewType.NUMBER;
                }
                return ViewType.TEXT;
            }
        }
        return rtn;
    }

}
