package xyz.erupt.k8s.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.fabric8.kubernetes.client.utils.YamlDumpSettingsBuilder;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.k8s.annotation.EruptK8s;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clients and resource handles for {@link EruptK8s} models, shared by the data service, the row
 * operations and the web terminal.
 * <p>
 * A record is addressed by its id: {@code name}, or {@code namespace/name} for models that span
 * namespaces ({@code namespace = "*"}, primary key {@code qualifiedName}).
 *
 * @author YuePeng
 */
@Service
public class EruptK8sClientService {

    public static final String ALL_NAMESPACES = "*";

    private final Map<String, KubernetesClient> clients = new ConcurrentHashMap<>();

    // kubectl-style YAML: strings unquoted unless a plain scalar would read as another type
    private final KubernetesSerialization yamlSerialization = new KubernetesSerialization(
            new ObjectMapper(), true, new YamlDumpSettingsBuilder().setMinimizeQuotes(true).build());

    private final Environment environment;

    public EruptK8sClientService(Environment environment) {
        this.environment = environment;
    }

    /**
     * Where one record lives; a null namespace means cluster-scoped or the client's default namespace.
     */
    public record Target(String namespace, String name) {

        public String id() {
            return null == namespace ? name : namespace + "/" + name;
        }

    }

    public EruptK8s eruptK8s(Class<?> eruptClass) {
        EruptK8s eruptK8s = eruptClass.getAnnotation(EruptK8s.class);
        if (null == eruptK8s) {
            throw new EruptWebApiRuntimeException("@EruptK8s annotation is missing on " + eruptClass.getSimpleName());
        }
        return eruptK8s;
    }

    public KubernetesClient client(EruptK8s eruptK8s) {
        String masterUrl = this.resolve(eruptK8s.masterUrl());
        String kubeConfigPath = this.resolve(eruptK8s.kubeConfigPath());
        String token = this.resolve(eruptK8s.token());
        String key = masterUrl + "|" + kubeConfigPath + "|" + Objects.hashCode(token);
        return clients.computeIfAbsent(key, k -> this.buildClient(masterUrl, kubeConfigPath, token));
    }

    // Annotation strings accept ${...} placeholders, so credentials come from configuration, never source
    private String resolve(String value) {
        return StringUtils.isBlank(value) ? "" : environment.resolvePlaceholders(value).trim();
    }

    // Loads the kubeconfig file explicitly rather than via System.setProperty — otherwise
    // two models pointing at different kubeconfigs race against each other on the JVM-wide property
    private KubernetesClient buildClient(String masterUrl, String kubeConfigPath, String token) {
        Config base;
        if (kubeConfigPath.isEmpty() && !masterUrl.isEmpty()) {
            // An explicit server stands alone: credentials from ~/.kube/config belong to another cluster
            base = Config.empty();
        } else if (kubeConfigPath.isEmpty()) {
            base = Config.autoConfigure(null);
        } else {
            try {
                base = Config.fromKubeconfig(Files.readString(Path.of(kubeConfigPath)));
            } catch (IOException e) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.operation_failed") + " → " + e.getMessage());
            }
        }
        ConfigBuilder config = new ConfigBuilder(base);
        if (!masterUrl.isEmpty()) config.withMasterUrl(masterUrl);
        if (!token.isEmpty()) config.withOauthToken(token);
        return new KubernetesClientBuilder().withConfig(config.build()).build();
    }

    public MixedOperation<GenericKubernetesResource, GenericKubernetesResourceList, Resource<GenericKubernetesResource>> operation(EruptK8s eruptK8s) {
        return this.client(eruptK8s).genericKubernetesResources(eruptK8s.apiVersion(), eruptK8s.kind());
    }

    /**
     * The namespace every record of the model lives in, or null when the model has none fixed.
     */
    public String fixedNamespace(EruptK8s eruptK8s) {
        String namespace = this.resolve(eruptK8s.namespace());
        return namespace.isEmpty() || ALL_NAMESPACES.equals(namespace) ? null : namespace;
    }

    public boolean allNamespaces(EruptK8s eruptK8s) {
        return ALL_NAMESPACES.equals(this.resolve(eruptK8s.namespace()));
    }

    /**
     * One list call. {@code namespace} narrows an all-namespaces model; a fixed namespace never widens.
     */
    public List<GenericKubernetesResource> list(EruptK8s eruptK8s, String namespace, String labelSelector) {
        MixedOperation<GenericKubernetesResource, GenericKubernetesResourceList, Resource<GenericKubernetesResource>> op = this.operation(eruptK8s);
        FilterWatchListDeletable<GenericKubernetesResource, GenericKubernetesResourceList, Resource<GenericKubernetesResource>> target;
        String fixed = this.fixedNamespace(eruptK8s);
        if (null != fixed) {
            target = op.inNamespace(fixed);
        } else if (this.allNamespaces(eruptK8s)) {
            target = StringUtils.isBlank(namespace) ? op.inAnyNamespace() : op.inNamespace(namespace);
        } else {
            target = op;
        }
        if (StringUtils.isNotBlank(labelSelector)) target = target.withLabelSelector(labelSelector);
        try {
            return target.list(new ListOptionsBuilder().withLimit((long) eruptK8s.maxItems()).build()).getItems();
        } catch (KubernetesClientException e) {
            throw wrap(e);
        }
    }

    public Target target(EruptK8s eruptK8s, Object id) {
        String value = String.valueOf(id);
        int slash = value.indexOf('/');
        if (slash > 0) {
            String namespace = value.substring(0, slash);
            String fixed = this.fixedNamespace(eruptK8s);
            // A fixed-namespace model never reaches outside its namespace, whatever the id says
            if (null != fixed && !fixed.equals(namespace)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.namespace_mismatch") + " → " + namespace);
            }
            return new Target(namespace, value.substring(slash + 1));
        }
        return new Target(this.fixedNamespace(eruptK8s), value);
    }

    public Resource<GenericKubernetesResource> resource(EruptK8s eruptK8s, Target target) {
        var op = this.operation(eruptK8s);
        return (null == target.namespace() ? op : op.inNamespace(target.namespace())).withName(target.name());
    }

    /**
     * The target behind a row object handed to a row operation.
     */
    public Target target(Object row) {
        EruptModel eruptModel = EruptCoreService.getErupt(row.getClass().getSimpleName());
        if (null == eruptModel) throw new EruptWebApiRuntimeException("Not an erupt model: " + row.getClass().getSimpleName());
        try {
            Field pk = ReflectUtil.findClassField(row.getClass(), eruptModel.getErupt().primaryKeyCol());
            pk.setAccessible(true);
            Object id = pk.get(row);
            if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.primary_key_missing"));
            return this.target(this.eruptK8s(row.getClass()), id);
        } catch (IllegalAccessException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    /**
     * A resource as {@code kubectl get -o yaml} prints it, without the server-side apply bookkeeping.
     */
    public String yaml(GenericKubernetesResource resource) {
        GenericKubernetesResource copy = yamlSerialization.clone(resource);
        if (null != copy.getMetadata()) copy.getMetadata().setManagedFields(null);
        String yaml = yamlSerialization.asYaml(copy);
        return yaml.startsWith("---\n") ? yaml.substring(4) : yaml;
    }

    public KubernetesSerialization serialization() {
        return yamlSerialization;
    }

    public static EruptWebApiRuntimeException wrap(KubernetesClientException e) {
        String detail = Objects.toString(null == e.getStatus() ? e.getMessage() : e.getStatus().getMessage(), e.getClass().getSimpleName());
        return new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.operation_failed") + " → " + detail);
    }

    @PreDestroy
    void closeClients() {
        clients.values().forEach(KubernetesClient::close);
        clients.clear();
    }

}
