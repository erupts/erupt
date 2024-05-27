package xyz.erupt.core.proxy;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;

import java.lang.annotation.Annotation;

/**
 * 注解代理
 *
 * @author YuePeng
 * date 2022/2/5 14:20
 */
public abstract class AnnotationProxy<A, PA> {

    // 原始注解
    public A rawAnnotation;

    // 代理后新注解
    public A proxyAnnotation;

    // 向上引用
    protected AnnotationProxy<PA, ?> parent;

    protected abstract Object invocation(MethodInvocation invocation);

    public A newProxy(A annotation) {
        return this.newProxy(annotation, null);
    }

    //创建注解注解代理类
    public A newProxy(A annotation, AnnotationProxy<PA, ?> parent) {
        this.parent = parent;
        this.rawAnnotation = annotation;
        ProxyFactory proxyFactory = new ProxyFactory(annotation);
        MethodInterceptor interceptor = this::invocation;
        proxyFactory.addAdvice(interceptor);
        this.proxyAnnotation = (A) proxyFactory.getProxy(this.getClass().getClassLoader());
        return this.proxyAnnotation;
    }

    // annotation method invoke
    @SneakyThrows
    public Object invoke(MethodInvocation invocation) {
        return invocation.getMethod().invoke(invocation.getThis());
    }

    public <T extends Annotation, R> boolean matchMethod(MethodInvocation invocation, SFunction<T, R> annoLambda) {
        return invocation.getMethod().getName().equals(LambdaSee.method(annoLambda));
    }

}
