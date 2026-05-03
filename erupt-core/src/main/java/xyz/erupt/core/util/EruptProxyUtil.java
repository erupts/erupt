package xyz.erupt.core.util;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;

import java.lang.annotation.Annotation;

/**
 * Annotation Proxy Util
 *
 * @author YuePeng
 * date 2026/4/23 20:20
 */
public class EruptProxyUtil {

    public static <A extends Annotation> A newProxy(A annotation, MethodInterceptor interceptor) {
        ProxyFactory proxyFactory = new ProxyFactory(annotation);
        proxyFactory.addAdvice(interceptor);
        return (A) proxyFactory.getProxy(EruptProxyUtil.class.getClassLoader());
    }

    public static <T extends Annotation, R> boolean matchMethod(MethodInvocation invocation, SFunction<T, R> annoLambda) {
        return invocation.getMethod().getName().equals(LambdaSee.method(annoLambda));
    }

}
