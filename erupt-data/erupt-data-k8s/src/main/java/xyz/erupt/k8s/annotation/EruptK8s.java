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
 * The primary key column supplies the resource {@code name}, or {@code namespace/name}
 * through a {@code qualifiedName} field for models that span namespaces. Writes go
 * through a {@code yaml} field holding the manifest, the way {@code kubectl create}
 * and {@code kubectl replace} take one; a model without that field is read-only
 * plus delete.
 * <p>
 * {@link #masterUrl()}, {@link #kubeConfigPath()}, {@link #token()} and
 * {@link #namespace()} accept {@code ${...}} placeholders, so cluster credentials
 * stay in configuration.
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
     * Target namespace. {@code "*"} lists every namespace (pair it with a
     * {@code qualifiedName} primary key); empty means cluster-scoped resources,
     * or the client's default namespace for namespaced ones.
     */
    String namespace() default "";

    /**
     * Master URL. When set without a kubeconfig path, the client connects with
     * only this URL and {@link #token()}. When all three connection settings are
     * empty, the standard discovery chain applies ({@code KUBECONFIG},
     * {@code ~/.kube/config}, in-cluster service account).
     */
    String masterUrl() default "";

    /**
     * Path to a kubeconfig file. Empty uses the default discovery chain.
     */
    String kubeConfigPath() default "";

    /**
     * Bearer token for authentication. Empty relies on the kubeconfig. Write a
     * placeholder such as {@code ${K8S_TOKEN:}}, never the token itself.
     */
    String token() default "";

    /**
     * Hard cap on the items materialised per list call — guards the admin JVM
     * against namespaces with thousands of resources.
     */
    int maxItems() default 1000;

}
