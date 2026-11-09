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
                return byDate(isDateEdit());
            }
            return this.rawAnnotation.operator();
        }
        return this.invoke(invocation);
    }

    /**
     * What an AUTO operator stands for: the legacy defaults, a range picker for dates, an exact match for everything
     * else. Shared with the completion of a node's build (PROTOCOL.md §12.3).
     */
    public static QueryExpression byDate(boolean date) {
        return date ? QueryExpression.RANGE : QueryExpression.EQ;
    }

    private boolean isDateEdit() {
        Field field = ProxyContext.get().getField();
        if (null == field) return false;
        // Disguised models (e.g. erupt-designer) may leave a context field without @EruptField.
        EruptField eruptField = field.getAnnotation(EruptField.class);
        if (null == eruptField) return false;
        if (EditType.AUTO == eruptField.edit().type()) {
            // Mirror the EditProxy AUTO inference for the date case.
            return EruptUtil.isDateField(field.getType().getSimpleName());
        }
        return EditType.DATE == eruptField.edit().type();
    }

}
