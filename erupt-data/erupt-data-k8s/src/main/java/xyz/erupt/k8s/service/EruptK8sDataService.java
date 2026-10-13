package xyz.erupt.k8s.service;

import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Kubernetes data source built on the fabric8 client. Models bind to a resource type via
 * {@link EruptK8s}; each item is projected to a flat row of metadata shortcuts plus the paths
 * declared through {@link EruptK8sField}. Filtering, sorting and paging run in memory, except the
 * namespace and label selector, which go to the API server.
 * <p>
 * Writes go through a {@value #YAML} field: a model that declares one can create and edit resources
 * from a manifest, the way {@code kubectl create} and {@code kubectl replace} do. Deletes work by id.
 *
 * @author YuePeng
 */
@Service
public class EruptK8sDataService extends EruptBeanDataService<Map<String, Object>> {

    public static final String DATA_PROCESSOR = "KUBERNETES";

    /**
     * Field holding the resource manifest; read in the detail view and written on add and edit.
     */
    public static final String YAML = "yaml";

    /**
     * Field holding {@code namespace/name}: the primary key for models that span namespaces.
     */
    public static final String QUALIFIED_NAME = "qualifiedName";

    /**
     * Search-only field whose value is sent to the API server as the label selector.
     */
    public static final String LABEL_SELECTOR = "labelSelector";

    private static final String NAMESPACE = "namespace";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptK8sDataService.class);
    }

    private final EruptK8sClientService clientService;

    public EruptK8sDataService(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    protected List<Map<String, Object>> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        EruptK8s eruptK8s = clientService.eruptK8s(eruptModel.getClazz());
        String namespace = null;
        String labelSelector = null;
        for (Condition condition : super.mergeConditions(eruptQuery)) {
            if (null == condition.getValue()) continue;
            if (LABEL_SELECTOR.equals(condition.getKey())) {
                labelSelector = condition.getValue().toString();
            } else if (NAMESPACE.equals(condition.getKey()) && condition.getExpression() == QueryExpression.EQ) {
                namespace = condition.getValue().toString();
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (GenericKubernetesResource item : clientService.list(eruptK8s, namespace, labelSelector)) {
            rows.add(this.toRow(eruptModel, item, false));
        }
        return rows;
    }

    // The label selector already ran on the server and matches no row value; keep it out of the in-memory filter
    @Override
    protected List<Condition> mergeConditions(EruptQuery eruptQuery) {
        List<Condition> conditions = new ArrayList<>(super.mergeConditions(eruptQuery));
        conditions.removeIf(it -> LABEL_SELECTOR.equals(it.getKey()));
        return conditions;
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        GenericKubernetesResource resource = this.fetch(eruptModel, id);
        if (null == resource) return null;
        Gson gson = GsonFactory.getGson();
        return gson.fromJson(gson.toJson(this.toRow(eruptModel, resource, true)), eruptModel.getClazz());
    }

    public GenericKubernetesResource fetch(EruptModel eruptModel, Object id) {
        EruptK8s eruptK8s = clientService.eruptK8s(eruptModel.getClazz());
        try {
            return clientService.resource(eruptK8s, clientService.target(eruptK8s, id)).get();
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptK8s eruptK8s = clientService.eruptK8s(eruptModel.getClazz());
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.primary_key_missing"));
        try {
            clientService.resource(eruptK8s, clientService.target(eruptK8s, id)).delete();
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptK8s eruptK8s = clientService.eruptK8s(eruptModel.getClazz());
        GenericKubernetesResource manifest = this.manifest(eruptModel, eruptK8s, object);
        String fixed = clientService.fixedNamespace(eruptK8s);
        if (null != fixed) {
            String namespace = manifest.getMetadata().getNamespace();
            if (StringUtils.isBlank(namespace)) manifest.getMetadata().setNamespace(fixed);
            else if (!fixed.equals(namespace)) throw this.mismatch("k8s.namespace_mismatch", namespace);
        }
        try {
            var op = clientService.operation(eruptK8s);
            String namespace = manifest.getMetadata().getNamespace();
            GenericKubernetesResource created = (StringUtils.isBlank(namespace) ? op : op.inNamespace(namespace)).resource(manifest).create();
            // Hand the server-assigned identity back, so afterAdd hooks and the add response see it
            this.copyRow(eruptModel, this.toRow(eruptModel, created, false), object);
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptK8s eruptK8s = clientService.eruptK8s(eruptModel.getClazz());
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.primary_key_missing"));
        EruptK8sClientService.Target target = clientService.target(eruptK8s, id);
        GenericKubernetesResource manifest = this.manifest(eruptModel, eruptK8s, object);
        ObjectMeta meta = manifest.getMetadata();
        if (!target.name().equals(meta.getName())) throw this.mismatch("k8s.name_mismatch", meta.getName());
        if (null != target.namespace()) {
            if (StringUtils.isBlank(meta.getNamespace())) meta.setNamespace(target.namespace());
            else if (!target.namespace().equals(meta.getNamespace())) throw this.mismatch("k8s.namespace_mismatch", meta.getNamespace());
        }
        try {
            // The manifest's resourceVersion makes this optimistic: a stale edit fails instead of overwriting
            var op = clientService.operation(eruptK8s);
            (null == meta.getNamespace() ? op : op.inNamespace(meta.getNamespace())).resource(manifest).update();
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
    }

    // The YAML field parsed into a resource of the model's own kind; any other kind is refused
    private GenericKubernetesResource manifest(EruptModel eruptModel, EruptK8s eruptK8s, Object object) {
        if (null == eruptModel.getEruptFieldMap().get(YAML)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.read_only_write"));
        }
        Object yaml = this.readValue(eruptModel, object, YAML);
        if (null == yaml || yaml.toString().isBlank()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.yaml_required"));
        }
        GenericKubernetesResource manifest;
        try {
            manifest = clientService.serialization().unmarshal(yaml.toString(), GenericKubernetesResource.class);
        } catch (RuntimeException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.yaml_invalid") + " → " + e.getMessage());
        }
        if (null == manifest || null == manifest.getMetadata() || StringUtils.isBlank(manifest.getMetadata().getName())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.yaml_invalid") + " → metadata.name");
        }
        if (!eruptK8s.apiVersion().equals(manifest.getApiVersion()) || !eruptK8s.kind().equals(manifest.getKind())) {
            throw this.mismatch("k8s.kind_mismatch", manifest.getApiVersion() + "/" + manifest.getKind());
        }
        return manifest;
    }

    private EruptWebApiRuntimeException mismatch(String key, String actual) {
        return new EruptWebApiRuntimeException(I18nTranslate.$translate(key) + " → " + actual);
    }

    private void copyRow(EruptModel eruptModel, Map<String, Object> row, Object target) {
        Gson gson = GsonFactory.getGson();
        Object projected = gson.fromJson(gson.toJson(row), eruptModel.getClazz());
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (YAML.equals(fieldModel.getFieldName())) continue;
            try {
                Field field = ReflectUtil.findClassField(eruptModel.getClazz(), fieldModel.getFieldName());
                field.setAccessible(true);
                field.set(target, field.get(projected));
            } catch (Exception ignore) {
                // Field types the projection cannot fill stay as submitted
            }
        }
    }

    public Map<String, Object> toRow(EruptModel eruptModel, GenericKubernetesResource resource, boolean detail) {
        Map<String, Object> raw = this.asMap(resource);
        Map<String, Object> row = new LinkedHashMap<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Field field = fieldModel.getField();
            EruptK8sField mapping = null == field ? null : field.getAnnotation(EruptK8sField.class);
            String name = fieldModel.getFieldName();
            Object value;
            if (null != mapping) value = this.readPath(raw, mapping.value());
            else if (YAML.equals(name)) value = detail ? clientService.yaml(resource) : null;
            else value = this.readShortcut(raw, name);
            row.put(name, value);
        }
        String pk = eruptModel.getErupt().primaryKeyCol();
        if (!row.containsKey(pk)) row.put(pk, this.readPath(raw, "metadata.name"));
        return row;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(GenericKubernetesResource resource) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("apiVersion", resource.getApiVersion());
        map.put("kind", resource.getKind());
        ObjectMeta meta = resource.getMetadata();
        if (null != meta) {
            // Every metadata field (ownerReferences, finalizers, deletionTimestamp, ...) is addressable by path
            Map<String, Object> metadata = clientService.serialization().convertValue(meta, Map.class);
            metadata.remove("managedFields");
            map.put("metadata", metadata);
        }
        Map<String, Object> additional = resource.getAdditionalProperties();
        if (null != additional) map.putAll(additional);
        return map;
    }

    @SuppressWarnings("unchecked")
    private Object readShortcut(Map<String, Object> raw, String fieldName) {
        return switch (fieldName) {
            case "name", "namespace", "uid", "resourceVersion", "creationTimestamp", "labels", "annotations" ->
                    this.readPath(raw, "metadata." + fieldName);
            case "kind", "apiVersion", "metadata", "spec", "status" -> raw.get(fieldName);
            case QUALIFIED_NAME -> {
                Object name = this.readPath(raw, "metadata.name");
                Object namespace = this.readPath(raw, "metadata.namespace");
                yield null == namespace ? name : namespace + "/" + name;
            }
            case LABEL_SELECTOR -> null;
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

}
