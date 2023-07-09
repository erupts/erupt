package xyz.erupt.core.proxy.erupt_field.type;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.proxy.AnnotationProxy;

/**
 * @author YuePeng
 * date 2023/6/24 19:29
 */
public class BoolTypeProxy extends AnnotationProxy<BoolType, Edit> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        switch (invocation.getMethod().getName()) {
            case "trueText":
                return I18nTranslate.$translate(this.rawAnnotation.trueText());
            case "falseText":
                return I18nTranslate.$translate(this.rawAnnotation.falseText());
        }
        return this.invoke(invocation);
    }

}
