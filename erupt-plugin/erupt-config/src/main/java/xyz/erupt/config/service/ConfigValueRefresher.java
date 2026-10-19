package xyz.erupt.config.service;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Keeps {@code @Value("${...}")} fields current: every singleton's placeholder fields are noted
 * as the bean is created, and set again from the environment after a refresh.
 *
 * @author YuePeng
 */
@Component
public class ConfigValueRefresher implements BeanPostProcessor {

    private record Injection(Object bean, Field field, String expression) {
    }

    private final List<Injection> injections = new CopyOnWriteArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object target = AopUtils.isAopProxy(bean) ? AopProxyUtils.getSingletonTarget(bean) : bean;
        if (null == target) return bean;
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Value value = field.getAnnotation(Value.class);
            // SpEL (#{...}) is left alone: only placeholders are re-resolved
            if (null != value && value.value().contains("${") && !value.value().contains("#{")) {
                injections.add(new Injection(target, field, value.value()));
            }
        });
        return bean;
    }

    public int refresh(Environment environment) {
        int changed = 0;
        for (Injection injection : injections) {
            String resolved;
            try {
                resolved = environment.resolveRequiredPlaceholders(injection.expression());
            } catch (IllegalArgumentException e) {
                continue;
            }
            Field field = injection.field();
            ReflectionUtils.makeAccessible(field);
            Object next = String.class == field.getType() ? resolved : DefaultConversionService.getSharedInstance().convert(resolved, field.getType());
            if (!Objects.equals(ReflectionUtils.getField(field, injection.bean()), next)) {
                ReflectionUtils.setField(field, injection.bean(), next);
                changed++;
            }
        }
        return changed;
    }

}
