package xyz.erupt.core.proxy.field;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.proxy.AnnotationProxy;

/**
 * @author YuePeng
 * date 2022/2/6 10:13
 */
public class ViewAnnotationProxy extends AnnotationProxy<View> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        Object rtn = this.invoke(invocation);
//        if ("type".equals(invocation.getMethod().getName())) {
//            if (ViewType.AUTO.name().equals(rtn.toString())) {
//                if (!AnnotationConst.EMPTY_STR.equals(this.eruptField.edit().title())) {
//                    switch (this.eruptField.edit().type()) {
//                        case ATTACHMENT:
//                            if (this.eruptField.edit().attachmentType().type() == AttachmentType.Type.IMAGE) {
//                                viewValues.put(TYPE, ViewType.IMAGE);
//                            } else {
//                                viewValues.put(TYPE, ViewType.ATTACHMENT);
//                            }
//                            continue;
//                        case CHOICE:
//                            viewValues.put(TYPE, ViewType.TEXT);
//                            continue;
//                        case HTML_EDITOR:
//                            viewValues.put(TYPE, ViewType.HTML);
//                            continue;
//                        case CODE_EDITOR:
//                            viewValues.put(TYPE, ViewType.CODE);
//                            continue;
//                        case MAP:
//                            viewValues.put(TYPE, ViewType.MAP);
//                            continue;
//                        case TAB_TABLE_ADD:
//                        case TAB_TREE:
//                        case TAB_TABLE_REFER:
//                        case CHECKBOX:
//                            viewValues.put(TYPE, ViewType.TAB_VIEW);
//                            continue;
//                    }
//                }
//                if (boolean.class.getSimpleName().equalsIgnoreCase(this.fieldReturnName.toLowerCase())) {
//                    return ViewType.BOOLEAN;
//                } else if (EruptUtil.isDateField(this.getFieldReturnName())) {
//                    return ViewType.DATE;
//                } else if (this.eruptField.edit().type() == EditType.DATE) {
//                    return ViewType.DATE;
//                } else if (JavaType.NUMBER.equals(this.fieldReturnName)) {
//                    return ViewType.NUMBER;
//                } else {
//                    return ViewType.TEXT;
//                }
//            }
//        }
        return rtn;
    }

}
