package xyz.erupt.core.proxy.erupt;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.ProxyContext;

/**
 * @author YuePeng
 * date 2022/2/7 18:22
 */
public class RowOperationProxy extends AnnotationProxy<RowOperation, Erupt> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        switch (invocation.getMethod().getName()) {
            case "code":
                if (AnnotationConst.EMPTY_STR.equals(this.rawAnnotation.code())) {
                    return Integer.toString(this.rawAnnotation.title().hashCode());
                }
                break;
            case "tip":
                return ProxyContext.translate(this.rawAnnotation.tip());
            case "title":
                return ProxyContext.translate(this.rawAnnotation.title());
        }
        return this.invoke(invocation);
    }
}
