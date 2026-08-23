package xyz.erupt.k8s.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps an erupt field to a JSON path inside the Kubernetes resource, e.g.
 * {@code spec.replicas}, {@code status.phase}, or
 * {@code spec.template.spec.containers[0].image}. Dot separates object keys and
 * {@code [n]} indexes into arrays.
 *
 * @author YuePeng
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptK8sField {

    /**
     * JSON path relative to the resource root.
     */
    String value();

}
