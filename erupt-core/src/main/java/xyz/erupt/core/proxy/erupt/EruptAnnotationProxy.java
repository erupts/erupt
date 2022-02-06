package xyz.erupt.core.proxy.erupt;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.core.proxy.AnnotationProxy;

/**
 * @author YuePeng
 * date 2022/2/5 14:19
 */
public class EruptAnnotationProxy extends AnnotationProxy<Erupt> {

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        if ("filter".equals(invocation.getMethod().getName())) {
            //TODO filter proxy
        }
        return this.invoke(invocation);
    }

}
