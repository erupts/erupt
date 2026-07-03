package xyz.erupt.core.proxy.erupt_field;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.util.EruptUtil;

import java.lang.reflect.Field;

/**
 * search proxy
 * <p>
 * Resolves the AUTO operator sentinel to a concrete operator so that
 * neither the frontend nor the query layer needs to be aware of AUTO.
 * The owning field is read from ProxyContext (not from parent): proxies are
 * pooled by value-equal annotations, so instance state must stay owner-free.
 *
 * @author YuePeng
 * date 2026/7/3
 */
public class SearchProxy extends AnnotationProxy<Search, Edit> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, Search::operator)) {
            if (QueryExpression.AUTO == this.rawAnnotation.operator()) {
                // Keep the legacy defaults: range picker for dates, exact match for everything else.
                return isDateEdit() ? QueryExpression.RANGE : QueryExpression.EQ;
            }
            return this.rawAnnotation.operator();
        }
        return this.invoke(invocation);
    }

    private boolean isDateEdit() {
        Field field = ProxyContext.get().getField();
        if (null == field) return false;
        Edit edit = field.getAnnotation(EruptField.class).edit();
        if (EditType.AUTO == edit.type()) {
            // Mirror the EditProxy AUTO inference for the date case.
            return EruptUtil.isDateField(field.getType().getSimpleName());
        }
        return EditType.DATE == edit.type();
    }

}
