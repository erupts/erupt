package xyz.erupt.core.proxy.erupt_field.type;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.util.EruptUtil;

import java.lang.reflect.Field;

/**
 * Resolves {@link DateType.Type#AUTO} from the owning field's Java type. Reads the field through
 * {@link ProxyContext} at call time and keeps no state, so the proxy can be pooled.
 */
public class DateTypeProxy extends AnnotationProxy<DateType, Edit> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, DateType::type)) {
            if (DateType.Type.AUTO == this.rawAnnotation.type()) {
                Field field = ProxyContext.get().getField();
                return null == field ? DateType.Type.DATE : EruptUtil.inferDateType(field);
            }
            return this.rawAnnotation.type();
        }
        return this.invoke(invocation);
    }

}
