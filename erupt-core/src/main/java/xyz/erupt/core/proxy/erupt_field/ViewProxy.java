package xyz.erupt.core.proxy.erupt_field;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.TypeUtil;

/**
 * @author YuePeng
 * date 2022/2/6 10:13
 */
public class ViewProxy extends AnnotationProxy<View, EruptField> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, View::type)) {
            if (ViewType.AUTO == this.rawAnnotation.type()) {
                Edit edit = this.parent.proxyAnnotation.edit();
                if (!AnnotationConst.EMPTY_STR.equals(edit.title())) {
                    switch (edit.type()) {
                        case ATTACHMENT:
                            if (edit.attachmentType().type() == AttachmentType.Type.IMAGE) {
                                return ViewType.IMAGE;
                            } else {
                                return ViewType.ATTACHMENT;
                            }
                        case CHOICE:
                            return ViewType.TEXT;
                        case DATE:
                            if (edit.dateType().type() == DateType.Type.DATE_TIME) {
                                return ViewType.DATE_TIME;
                            } else {
                                return ViewType.DATE;
                            }
                        case HTML_EDITOR:
                            return ViewType.HTML;
                        case CODE_EDITOR:
                            return ViewType.CODE;
                        case MAP:
                            return ViewType.MAP;
                        case COLOR:
                            return ViewType.COLOR;
                        case TAB_TABLE_ADD:
                        case TAB_TREE:
                        case TAB_TABLE_REFER:
                        case CHECKBOX:
                            return ViewType.TAB_VIEW;
                    }
                }
                String returnType = ProxyContext.get().getField().getType().getSimpleName();
                if (boolean.class.getSimpleName().equalsIgnoreCase(returnType.toLowerCase())) {
                    return ViewType.BOOLEAN;
                } else if (EruptUtil.isDateField(returnType)) {
                    return ViewType.DATE;
                } else if (TypeUtil.isNumberType(returnType)) {
                    return ViewType.NUMBER;
                }
                return ViewType.TEXT;
            }
        } else if (super.matchMethod(invocation, View::title)) {
            return ProxyContext.translate(this.rawAnnotation.title());
        } else if (super.matchMethod(invocation, View::desc)) {
            return ProxyContext.translate(this.rawAnnotation.desc());
        }
        return this.invoke(invocation);
    }

}
