package xyz.erupt.k8s.service;

import xyz.erupt.core.view.Page;
import java.util.concurrent.ConcurrentHashMap;
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
        List<EruptK8sClientService.FieldTerm> fields = new ArrayList<>();
        for (Condition condition : super.mergeConditions(eruptQuery)) {
            if (null == condition.getValue()) continue;
            if (LABEL_SELECTOR.equals(condition.getKey())) {
                labelSelector = condition.getValue().toString();
            } else if (NAMESPACE.equals(condition.getKey()) && condition.getExpression() == QueryExpression.EQ) {
                namespace = condition.getValue().toString();
            } else {
                // also checked in memory afterwards; the selector only shrinks what is read
                Optional.ofNullable(this.fieldTerm(eruptModel, condition)).ifPresent(fields::add);
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (GenericKubernetesResource item : clientService.list(eruptK8s, namespace, labelSelector, fields)) {
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

    // ------------------------------------------------------------------ paging

    // A continue token stays valid until the server compacts (5 minutes by default); keep it a little less
    private static final long CURSOR_TTL_MILLIS = 4 * 60 * 1000;

    private static final int MAX_CURSORS = 2000;

    private record Cursor(String token, long expireAt) {
    }

    // query + page size + page index -> the continue token that reads that page
    private final Map<String, Cursor> cursors = new ConcurrentHashMap<>();

    /**
     * Three ways to answer a list, cheapest first:
     * <ul>
     *   <li>an id condition: one GET (scope checks and row operations ask this way);</li>
     *   <li>only namespace and label selector, no sort: the API server pages with limit / continue,
     *       so a page reads one page of resources however many the cluster holds;</li>
     *   <li>anything else: up to {@code maxItems} resources, filtered, sorted and paged in memory.</li>
     * </ul>
     */
    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        EruptK8s eruptK8s = clientService.eruptK8s(eruptModel.getClazz());
        String pk = eruptModel.getErupt().primaryKeyCol();
        String namespace = null;
        String labelSelector = null;
        Object id = null;
        List<Condition> rest = new ArrayList<>();
        List<EruptK8sClientService.FieldTerm> fields = new ArrayList<>();
        for (Condition condition : super.mergeConditions(eruptQuery)) {
            if (null == condition.getValue()) continue;
            EruptK8sClientService.FieldTerm term = this.fieldTerm(eruptModel, condition);
            if (LABEL_SELECTOR.equals(condition.getKey())) labelSelector = condition.getValue().toString();
            else if (NAMESPACE.equals(condition.getKey()) && condition.getExpression() == QueryExpression.EQ) namespace = condition.getValue().toString();
            else if (pk.equals(condition.getKey()) && condition.getExpression() == QueryExpression.EQ) id = condition.getValue();
            else if (null != term) fields.add(term);
            else rest.add(condition);
        }
        if (null != id && rest.isEmpty() && fields.isEmpty() && null == labelSelector) return this.byId(eruptModel, page, id, namespace);
        boolean sorted = (null != page.getSort() && !page.getSort().isEmpty()) || StringUtils.isNotBlank(eruptModel.getErupt().orderBy());
        if (!rest.isEmpty() || sorted || null != id) return super.queryList(eruptModel, page, eruptQuery);
        return this.serverPage(eruptModel, eruptK8s, page, namespace, labelSelector, fields);
    }

    // An equals / not-equals search on a field the API server can select on, as a field selector term
    private EruptK8sClientService.FieldTerm fieldTerm(EruptModel eruptModel, Condition condition) {
        if (condition.getExpression() != QueryExpression.EQ && condition.getExpression() != QueryExpression.NEQ) return null;
        if (condition.getValue() instanceof Collection) return null;
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(condition.getKey());
        Field field = null == fieldModel ? null : fieldModel.getField();
        EruptK8sField mapping = null == field ? null : field.getAnnotation(EruptK8sField.class);
        if (null == mapping || !mapping.fieldSelector()) return null;
        return new EruptK8sClientService.FieldTerm(mapping.value(), condition.getValue().toString(), condition.getExpression() == QueryExpression.NEQ);
    }

    private Page byId(EruptModel eruptModel, Page page, Object id, String namespace) {
        GenericKubernetesResource resource;
        try {
            resource = this.fetch(eruptModel, id);
        } catch (EruptWebApiRuntimeException e) {
            resource = null;
        }
        boolean visible = null != resource && (null == namespace || namespace.equals(resource.getMetadata().getNamespace()));
        page.setList(visible ? new ArrayList<>(List.of(this.toRow(eruptModel, resource, false))) : new ArrayList<>());
        page.setTotal((long) page.getList().size());
        return page;
    }

    private Page serverPage(EruptModel eruptModel, EruptK8s eruptK8s, Page page, String namespace, String labelSelector,
                            List<EruptK8sClientService.FieldTerm> fields) {
        int size = page.getPageSize();
        int index = Math.max(page.getPageIndex(), 1);
        String query = eruptModel.getEruptName() + "|" + clientService.context(eruptK8s) + "|" + namespace + "|" + labelSelector
                + "|" + fields + "|" + size + "|";
        EruptK8sClientService.Slice slice;
        try {
            slice = this.walk(eruptK8s, query, namespace, labelSelector, fields, size, index);
        } catch (KubernetesClientException e) {
            if (e.getCode() != 410) throw EruptK8sClientService.wrap(e);
            // the cursor expired between pages: read again from the first page
            cursors.keySet().removeIf(key -> key.startsWith(query));
            try {
                slice = this.walk(eruptK8s, query, namespace, labelSelector, fields, size, index);
            } catch (KubernetesClientException again) {
                throw EruptK8sClientService.wrap(again);
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (GenericKubernetesResource item : slice.items()) rows.add(this.toRow(eruptModel, item, false));
        long before = (long) (index - 1) * size;
        long total;
        if (null == slice.next()) total = before + rows.size();
        else if (null != slice.remaining()) total = before + rows.size() + slice.remaining();
        // the server does not count (it never does under a label selector): promise one more page
        else total = before + rows.size() + size;
        page.setList(rows);
        page.setTotal(total);
        return page;
    }

    // Reads page index, starting from the nearest page whose continue token is cached
    private EruptK8sClientService.Slice walk(EruptK8s eruptK8s, String query, String namespace, String labelSelector,
                                             List<EruptK8sClientService.FieldTerm> fields, int size, int index) {
        long now = System.currentTimeMillis();
        if (cursors.size() > MAX_CURSORS) cursors.values().removeIf(it -> it.expireAt() < now);
        int from = 1;
        String token = null;
        for (int i = index; i > 1; i--) {
            Cursor cursor = cursors.get(query + i);
            if (null != cursor && cursor.expireAt() > now) {
                from = i;
                token = cursor.token();
                break;
            }
        }
        EruptK8sClientService.Slice slice = clientService.slice(eruptK8s, namespace, labelSelector, fields, size, token);
        int at = from;
        for (; at < index && null != slice.next(); at++) {
            cursors.put(query + (at + 1), new Cursor(slice.next(), now + CURSOR_TTL_MILLIS));
            slice = clientService.slice(eruptK8s, namespace, labelSelector, fields, size, slice.next());
        }
        // asked past the last page: there is nothing there
        if (at < index) return new EruptK8sClientService.Slice(List.of(), null, null);
        if (null != slice.next()) cursors.put(query + (index + 1), new Cursor(slice.next(), now + CURSOR_TTL_MILLIS));
        return slice;
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
