package xyz.erupt.core.proxy;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Field;

/**
 * 注解代理
 *
 * @author YuePeng
 * date 2022/2/5 14:20
 */
public abstract class AnnotationProxy<A, PA> {

    // 注解修饰的类
    protected Class<?> clazz;

    // 注解修饰的字段
    protected Field field;

    // 原始注解
    public A rawAnnotation;

    // 代理后新注解
    public A proxyAnnotation;

    // 向上引用
    protected AnnotationProxy<PA, ?> parent;

    protected abstract Object invocation(MethodInvocation invocation);

    //创建注解注解代理类
    public A newProxy(A annotation, AnnotationProxy<PA, ?> parent, Class<?> clazz, Field field) {
        this.clazz = clazz;
        this.field = field;
        this.parent = parent;
        this.rawAnnotation = annotation;
        ProxyFactory proxyFactory = new ProxyFactory(annotation);
        MethodInterceptor interceptor = this::invocation;
        proxyFactory.addAdvice(interceptor);
        this.proxyAnnotation = (A) proxyFactory.getProxy(this.getClass().getClassLoader());
        return this.proxyAnnotation;
    }

    public A newProxy(A annotation, AnnotationProxy<PA, ?> parent, Field field) {
        return this.newProxy(annotation, parent, null, field);
    }

    public A newProxy(A annotation, AnnotationProxy<PA, ?> parent, Class<?> clazz) {
        return this.newProxy(annotation, parent, clazz, null);
    }

    // annotation method invoke
    @SneakyThrows
    public Object invoke(MethodInvocation invocation) {
        return invocation.getMethod().invoke(invocation.getThis());
    }

}
