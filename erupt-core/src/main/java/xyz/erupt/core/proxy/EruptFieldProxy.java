package xyz.erupt.core.proxy;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.proxy.erupt_field.EditProxy;
import xyz.erupt.core.proxy.erupt_field.ViewProxy;
import xyz.erupt.core.tpl.EruptTpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/2/5 14:19
 */
public class EruptFieldProxy extends AnnotationProxy<EruptField, Void> {

    private static final EruptField tplEruptField;

    static {
        try {
            tplEruptField = EruptTpl.class.getField(EruptField.class.getSimpleName()).getAnnotation(EruptField.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        switch (invocation.getMethod().getName()) {
            case "views":
                View[] views = this.rawAnnotation.views();
                List<View> proxyViews = new ArrayList<>();
                for (View view : views) {
                    if (ExprInvoke.getExpr(view.ifRender())) {
                        proxyViews.add(AnnotationProxyPool.getOrPut(view, annotation ->
                                new ViewProxy().newProxy(annotation, this)
                        ));
                    }
                }
                return proxyViews.toArray(new View[0]);
            case "edit":
                Edit edit = this.rawAnnotation.edit();
                if (ExprInvoke.getExpr(edit.ifRender())) {
                    return AnnotationProxyPool.getOrPut(edit, annotation ->
                            new EditProxy().newProxy(annotation, this)
                    );
                } else {
                    return tplEruptField.edit();
                }
        }
        return this.invoke(invocation);
    }

}
