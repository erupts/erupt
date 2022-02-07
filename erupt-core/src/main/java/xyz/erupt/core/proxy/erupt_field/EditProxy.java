package xyz.erupt.core.proxy.erupt_field;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.AnnotationProxyPool;
import xyz.erupt.core.proxy.FilterProxy;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.TypeUtil;

/**
 * @author YuePeng
 * date 2022/2/6 10:13
 */
public class EditProxy extends AnnotationProxy<Edit> {

    private final AnnotationProxy<Filter> filterProxy = new FilterProxy();

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        Object rtn = this.invoke(invocation);
        if ("type".equals(invocation.getMethod().getName())) {
            if (EditType.AUTO.name().equals(rtn.toString())) {
                // 根据返回值类型推断
                String returnType = this.field.getType().getSimpleName();
                if (boolean.class.getSimpleName().equalsIgnoreCase(returnType)) {
                    return EditType.BOOLEAN;
                } else if (TypeUtil.isNumberType(returnType)) {
                    return EditType.NUMBER;
                } else if (EruptUtil.isDateField(returnType)) {
                    return EditType.DATE;
                } else if (ArrayUtils.contains(AnnotationUtil.getEditTypeMapping(EditType.TEXTAREA).nameInfer(), returnType)) {
                    return EditType.TEXTAREA; //属性名推断
                }
                return EditType.INPUT;
            }
        } else if ("filter".equals(invocation.getMethod().getName())) {
            Filter[] filters = (Filter[]) this.invoke(invocation);
            Filter[] proxyFilters = new Filter[filters.length];
            for (int i = 0; i < filters.length; i++) {
                proxyFilters[i] = AnnotationProxyPool.getOrPut(filters[i], filter ->
                        filterProxy.newProxy(filter, this, this.clazz));
            }
            return proxyFilters;
        }
        return rtn;
    }

}
