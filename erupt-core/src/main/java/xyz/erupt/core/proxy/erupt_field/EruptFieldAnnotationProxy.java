package xyz.erupt.core.proxy.erupt_field;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.proxy.AnnotationProxy;

/**
 * @author YuePeng
 * date 2022/2/5 14:19
 */
public class EruptFieldAnnotationProxy extends AnnotationProxy<EruptField> {

    private final AnnotationProxy<Edit> editAnnotationProxy = new EditAnnotationProxy();

    private final AnnotationProxy<View> viewAnnotationProxy = new ViewAnnotationProxy();

    //代理池对象
//    private final Map<Edit, Edit> editPool = new HashMap<>();
//
//    private final Map<View, View> viewPool = new HashMap<>();

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        if (Edit.class.getSimpleName().equals(invocation.getMethod().getReturnType().getSimpleName())) {
            return editAnnotationProxy.newProxy((Edit) this.invoke(invocation), this.field);
        } else if ("views".equals(invocation.getMethod().getName())) {
            View[] views = (View[]) this.invoke(invocation);
            View[] proxyViews = new View[views.length];
            for (int i = 0; i < views.length; i++) {
                proxyViews[i] = viewAnnotationProxy.newProxy(views[i], this.field);
            }
            return proxyViews;
        } else {
            return this.invoke(invocation);
        }
    }

}
