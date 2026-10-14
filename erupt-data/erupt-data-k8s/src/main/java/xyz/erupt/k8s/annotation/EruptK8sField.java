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

    /**
     * The API server can filter on this path ({@code kubectl get --field-selector}): an equals or
     * not-equals search on the field is sent as a field selector, so the list stays paged on the
     * server. Only a few paths per kind qualify, e.g. a Pod's {@code spec.nodeName} and
     * {@code status.phase}; marking any other path makes the API server refuse the list.
     */
    boolean fieldSelector() default false;

}
