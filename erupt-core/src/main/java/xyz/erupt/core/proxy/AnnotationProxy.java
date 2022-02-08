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
public abstract class AnnotationProxy<Annotation, Parent> {

    // 注解修饰的类
    protected Class<?> clazz;

    // 注解修饰的字段
    protected Field field;

    // 原始注解
    public Annotation rawAnnotation;

    // 代理后新注解
    public Annotation proxyAnnotation;

    // 向上引用
    protected AnnotationProxy<Parent, ?> parent;

    protected abstract Object invocation(MethodInvocation invocation);

    //创建注解注解代理类
    public Annotation newProxy(Annotation annotation, AnnotationProxy<Parent, ?> parent, Class<?> clazz, Field field) {
        this.clazz = clazz;
        this.field = field;
        this.parent = parent;
        this.rawAnnotation = annotation;
        ProxyFactory proxyFactory = new ProxyFactory(annotation);
        MethodInterceptor interceptor = this::invocation;
        proxyFactory.addAdvice(interceptor);
        Object proxy = proxyFactory.getProxy(AnnotationProxy.class.getClassLoader());
        this.proxyAnnotation = (Annotation) proxy;
        return this.proxyAnnotation;
    }

    public Annotation newProxy(Annotation annotation, AnnotationProxy<Parent, ?> parent, Field field) {
        return this.newProxy(annotation, parent, null, field);
    }

    public Annotation newProxy(Annotation annotation, AnnotationProxy<Parent, ?> parent, Class<?> clazz) {
        return this.newProxy(annotation, parent, clazz, null);
    }

    // annotation method invoke
    @SneakyThrows
    public Object invoke(MethodInvocation invocation) {
        return invocation.getMethod().invoke(invocation.getThis());
    }

}
