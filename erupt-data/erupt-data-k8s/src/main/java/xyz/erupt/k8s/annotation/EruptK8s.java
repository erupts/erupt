package xyz.erupt.k8s.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a Kubernetes resource type. Place alongside
 * {@code @EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)}.
 * <p>
 * Fields on the model map to values inside each resource. Common
 * {@code metadata.*} attributes (name, namespace, uid, resourceVersion,
 * creationTimestamp, labels, annotations) are exposed by matching field name;
 * for anything nested — e.g. {@code spec.replicas} — declare the mapping with
 * {@link EruptK8sField}.
 * <p>
 * The primary key column supplies the resource {@code name}. This module is
 * read-only (plus delete) by design: constructing arbitrary K8s specs from a
 * form is error-prone, so writes are best expressed as YAML in the client.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptK8s {

    /**
     * API group / version, e.g. {@code v1}, {@code apps/v1}, {@code batch/v1}.
     */
    String apiVersion();

    /**
     * Resource kind, e.g. {@code Pod}, {@code Deployment}, {@code ConfigMap}.
     */
    String kind();

    /**
     * Target namespace. Empty means cluster-scoped (or all namespaces for
     * namespaced resources when the server permits).
     */
    String namespace() default "";

    /**
     * Master URL. Empty falls back to the standard client discovery chain
     * ({@code KUBECONFIG}, {@code ~/.kube/config}, in-cluster service account).
     */
    String masterUrl() default "";

    /**
     * Path to a kubeconfig file. Empty uses the default discovery chain.
     */
    String kubeConfigPath() default "";

    /**
     * Bearer token for authentication. Empty relies on the kubeconfig.
     */
    String token() default "";

    /**
     * Hard cap on the items materialised per list call — guards the admin JVM
     * against namespaces with thousands of resources.
     */
    int maxItems() default 1000;

}
