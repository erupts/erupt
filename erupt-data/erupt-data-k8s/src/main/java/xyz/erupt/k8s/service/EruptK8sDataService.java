package xyz.erupt.k8s.service;

import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kubernetes data source built on the fabric8 client. Models bind to a
 * resource type via {@link EruptK8s(apiVersion, kind)}; each list item is
 * projected to a flat row containing metadata shortcuts (name / namespace /
 * uid / resourceVersion / creationTimestamp / labels / annotations) plus any
 * additional paths declared through {@link EruptK8sField}. Filtering, sorting
 * and paging are handled in-memory by the base class.
 * <p>
 * Adds and edits are intentionally not supported: building a K8s spec through
 * an admin form is fragile compared with authoring YAML. Deletes work by
 * resource name.
 *
 * @author YuePeng
 */
@Service
public class EruptK8sDataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "KUBERNETES";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptK8sDataService.class);
    }

    private final Map<String, KubernetesClient> clients = new ConcurrentHashMap<>();

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptK8s eruptK8s = this.eruptK8s(eruptModel);
        try {
            List<GenericKubernetesResource> items = this.operation(eruptK8s).list().getItems();
            int cap = Math.min(items.size(), eruptK8s.maxItems());
            List<Map<String, Object>> rows = new ArrayList<>(cap);
            for (int i = 0; i < cap; i++) rows.add(this.toRow(eruptModel, items.get(i)));
            return rows;
        } catch (KubernetesClientException e) {
            throw this.wrap(e);
        }
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptK8s eruptK8s = this.eruptK8s(eruptModel);
        try {
            GenericKubernetesResource resource = this.operation(eruptK8s).withName(String.valueOf(id)).get();
            if (null == resource) return null;
            Gson gson = GsonFactory.getGson();
            return gson.fromJson(gson.toJson(this.toRow(eruptModel, resource)), eruptModel.getClazz());
        } catch (KubernetesClientException e) {
            throw this.wrap(e);
        }
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptK8s eruptK8s = this.eruptK8s(eruptModel);
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.primary_key_missing"));
        try {
            this.operation(eruptK8s).withName(String.valueOf(id)).delete();
        } catch (KubernetesClientException e) {
            throw this.wrap(e);
        }
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.read_only_write"));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.read_only_write"));
    }

    private EruptK8s eruptK8s(EruptModel eruptModel) {
        EruptK8s eruptK8s = eruptModel.getClazz().getAnnotation(EruptK8s.class);
        if (null == eruptK8s) {
            throw new EruptWebApiRuntimeException("@EruptK8s annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptK8s;
    }

    private NonNamespaceOperation<GenericKubernetesResource, GenericKubernetesResourceList,
            Resource<GenericKubernetesResource>> operation(EruptK8s eruptK8s) {
        MixedOperation<GenericKubernetesResource, GenericKubernetesResourceList,
                Resource<GenericKubernetesResource>> op = this.client(eruptK8s)
                .genericKubernetesResources(eruptK8s.apiVersion(), eruptK8s.kind());
        return eruptK8s.namespace().isEmpty() ? op : op.inNamespace(eruptK8s.namespace());
    }

    private KubernetesClient client(EruptK8s eruptK8s) {
        String key = eruptK8s.masterUrl() + "|" + eruptK8s.kubeConfigPath() + "|" + eruptK8s.token();
        return clients.computeIfAbsent(key, k -> this.buildClient(eruptK8s));
    }

    // Loads the kubeconfig file explicitly rather than via System.setProperty — otherwise
    // two models pointing at different kubeconfigs race against each other on the JVM-wide property
    private KubernetesClient buildClient(EruptK8s eruptK8s) {
        Config base;
        if (eruptK8s.kubeConfigPath().isEmpty()) {
            base = Config.autoConfigure(null);
        } else {
            try {
                base = Config.fromKubeconfig(Files.readString(Path.of(eruptK8s.kubeConfigPath())));
            } catch (IOException e) {
                throw new EruptWebApiRuntimeException(
                        I18nTranslate.$translate("k8s.operation_failed") + " → " + e.getMessage());
            }
        }
        ConfigBuilder config = new ConfigBuilder(base);
        if (!eruptK8s.masterUrl().isEmpty()) config.withMasterUrl(eruptK8s.masterUrl());
        if (!eruptK8s.token().isEmpty()) config.withOauthToken(eruptK8s.token());
        return new KubernetesClientBuilder().withConfig(config.build()).build();
    }

    private Map<String, Object> toRow(EruptModel eruptModel, GenericKubernetesResource resource) {
        Map<String, Object> raw = this.asMap(resource);
        Map<String, Object> row = new LinkedHashMap<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Field field = fieldModel.getField();
            EruptK8sField mapping = null == field ? null : field.getAnnotation(EruptK8sField.class);
            Object value = null != mapping ? this.readPath(raw, mapping.value())
                    : this.readShortcut(raw, fieldModel.getFieldName());
            row.put(fieldModel.getFieldName(), value);
        }
        String pk = eruptModel.getErupt().primaryKeyCol();
        if (!row.containsKey(pk)) row.put(pk, this.readPath(raw, "metadata.name"));
        return row;
    }

    // Build the flat map directly from fabric8's typed getters — avoids a Jackson-then-Gson
    // JSON round-trip per resource, which is the hot path on every list render
    private Map<String, Object> asMap(GenericKubernetesResource resource) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("apiVersion", resource.getApiVersion());
        map.put("kind", resource.getKind());
        ObjectMeta meta = resource.getMetadata();
        if (null != meta) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("name", meta.getName());
            metadata.put("namespace", meta.getNamespace());
            metadata.put("uid", meta.getUid());
            metadata.put("resourceVersion", meta.getResourceVersion());
            metadata.put("creationTimestamp", meta.getCreationTimestamp());
            metadata.put("generation", meta.getGeneration());
            metadata.put("labels", meta.getLabels());
            metadata.put("annotations", meta.getAnnotations());
            map.put("metadata", metadata);
        }
        Map<String, Object> additional = resource.getAdditionalProperties();
        if (null != additional) map.putAll(additional);
        return map;
    }

    private Object readShortcut(Map<String, Object> raw, String fieldName) {
        return switch (fieldName) {
            case "name", "namespace", "uid", "resourceVersion", "creationTimestamp", "labels", "annotations" ->
                    this.readPath(raw, "metadata." + fieldName);
            case "kind", "apiVersion", "metadata", "spec", "status" -> raw.get(fieldName);
            default -> this.readPath(raw, fieldName);
        };
    }

    @SuppressWarnings("unchecked")
    private Object readPath(Map<String, Object> raw, String path) {
        Object cursor = raw;
        for (String segment : path.split("\\.")) {
            if (null == cursor) return null;
            String key = segment;
            List<Integer> indexes = new ArrayList<>();
            while (key.endsWith("]")) {
                int open = key.lastIndexOf('[');
                if (open < 0) break;
                indexes.add(0, Integer.parseInt(key.substring(open + 1, key.length() - 1)));
                key = key.substring(0, open);
            }
            if (!key.isEmpty()) {
                if (!(cursor instanceof Map)) return null;
                cursor = ((Map<String, Object>) cursor).get(key);
            }
            for (Integer idx : indexes) {
                if (!(cursor instanceof List)) return null;
                List<Object> list = (List<Object>) cursor;
                cursor = idx >= 0 && idx < list.size() ? list.get(idx) : null;
            }
        }
        return cursor;
    }

    private EruptWebApiRuntimeException wrap(KubernetesClientException e) {
        String detail = Objects.toString(e.getMessage(), e.getClass().getSimpleName());
        return new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.operation_failed") + " → " + detail);
    }

    @PreDestroy
    void closeClients() {
        clients.values().forEach(KubernetesClient::close);
        clients.clear();
    }

}
