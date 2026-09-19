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
        if (super.matchMethod(invocation, BoolType::trueText)) {
            return I18nTranslate.$translate(this.rawAnnotation.trueText());
        } else if (super.matchMethod(invocation, BoolType::falseText)) {
            return I18nTranslate.$translate(this.rawAnnotation.falseText());
        } else if (super.matchMethod(invocation, BoolType::type)) {
            if (BoolType.Type.AUTO == this.rawAnnotation.type()) {
                // A required bool has no unset state left to express, so it renders as a two-state
                // switch; an optional one keeps radios so "not answered" stays distinguishable.
                // The client only ever sees RADIO or SWITCH.
                return this.parent.rawAnnotation.notNull() ? BoolType.Type.SWITCH : BoolType.Type.RADIO;
            }
            return this.rawAnnotation.type();
        }
        return this.invoke(invocation);
    }

}
