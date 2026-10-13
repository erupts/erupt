# erupt-data-k8s

Kubernetes resources as erupt models, built on the fabric8 kubernetes-client (7.x, JDK HttpClient transport). Bind a `@Erupt` model to a resource kind (Pod, Deployment, ConfigMap, Node, any custom resource) and Erupt gives you a filtered, searchable, permissioned admin view of it, with arbitrary field extraction from `spec` / `status`.

For a ready-made Kubernetes admin, with no models to write, use [erupt-k8s-dashboard](../../erupt-plugin/erupt-k8s-dashboard). It is built on this module. Use this module directly to model resources the dashboard does not ship, such as custom resources.

On top of browsing:

| Capability | How |
|---|---|
| Create / edit from YAML | declare a `yaml` field (`kubectl create` / `kubectl replace`) |
| Delete | built in |
| Scale | `K8sScaleHandler` row operation (`kubectl scale`) |
| Rollout restart | `K8sRestartHandler` row operation (`kubectl rollout restart`) |
| View YAML | `K8sYamlHandler` row operation (`kubectl get -o yaml`) |
| Pod logs, last N lines | `K8sPodLogHandler` row operation (`kubectl logs --tail`) |
| Live logs / shell | web terminal in [erupt-k8s-dashboard](../../erupt-plugin/erupt-k8s-dashboard) |
| Namespace dropdown | `K8sNamespaceFetchHandler` choice handler |
| Server-side filtering | `namespace` and `labelSelector` searches go to the API server |

## Annotations

### `@EruptK8s` (type-level)

| Attribute        | Default | Description |
|------------------|---------|-------------|
| `apiVersion`     | —       | API group / version, e.g. `v1`, `apps/v1`, `batch/v1` |
| `kind`           | —       | Resource kind, e.g. `Pod`, `Deployment`, `ConfigMap` |
| `namespace`      | `""`    | Fixed namespace; `"*"` for all namespaces; empty for cluster-scoped kinds or the client's default namespace |
| `masterUrl`      | `""`    | API server URL; set alone, the client ignores `~/.kube/config` |
| `kubeConfigPath` | `""`    | Path to a kubeconfig file |
| `token`          | `""`    | Bearer token |
| `maxItems`       | `1000`  | Page size sent to the API server per list call |

All string attributes accept `${...}` placeholders. Keep credentials in configuration:

```java
@EruptK8s(apiVersion = "apps/v1", kind = "Deployment", namespace = "${k8s.namespace:default}",
          masterUrl = "${k8s.master-url:}", token = "${K8S_TOKEN:}")
```

If `kubeConfigPath`, `masterUrl` and `token` are all empty, the client uses the standard discovery chain: `KUBECONFIG`, `~/.kube/config`, then the in-cluster service account.

### `@EruptK8sField(path)` (field-level)

Maps a field to a path inside the resource, e.g. `spec.replicas`, `status.phase`, `spec.template.spec.containers[0].image`, `metadata.ownerReferences[0].name`. Dot separates keys and `[n]` indexes into arrays.

### Field shortcuts

These field names resolve without `@EruptK8sField`:

| Field | Value |
|---|---|
| `name`, `namespace`, `uid`, `resourceVersion`, `creationTimestamp`, `labels`, `annotations` | `metadata.*` |
| `kind`, `apiVersion`, `metadata`, `spec`, `status` | top-level sections |
| `qualifiedName` (`EruptK8sDataService.QUALIFIED_NAME`) | `namespace/name`; the primary key for `namespace = "*"` models |
| `yaml` (`EruptK8sDataService.YAML`) | the manifest, filled in the detail view only, written on add and edit |
| `labelSelector` (`EruptK8sDataService.LABEL_SELECTOR`) | search-only; sent to the API server as the label selector |

## Example — Deployments with YAML editing and workload actions

```java
@Getter
@Setter
@Erupt(name = "Deployments", primaryKeyCol = "name", rowOperation = {
        @RowOperation(code = "scale", title = "Scale", mode = RowOperation.Mode.SINGLE,
                eruptClass = K8sScaleForm.class, operationHandler = K8sScaleHandler.class),
        @RowOperation(code = "restart", title = "Restart", operationHandler = K8sRestartHandler.class)
})
@EruptK8s(apiVersion = "apps/v1", kind = "Deployment", namespace = "default")
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class K8sDeployment {

    @EruptField(views = @View(title = "Name"))
    private String name;

    @EruptK8sField("spec.replicas")
    @EruptField(views = @View(title = "Replicas"))
    private Integer replicas;

    @EruptK8sField("status.readyReplicas")
    @EruptField(views = @View(title = "Ready"))
    private Integer readyReplicas;

    @EruptK8sField("spec.template.spec.containers[0].image")
    @EruptField(views = @View(title = "Image"))
    private String image;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

    // Form only, no column: lists skip it, the edit form loads the live manifest
    @EruptField(edit = @Edit(title = "YAML", notNull = true, type = EditType.CODE_EDITOR,
            codeEditType = @CodeEditorType(language = "yaml", height = 520)))
    private String yaml;
}
```

Add takes a manifest of the model's own `apiVersion`/`kind`. A manifest without a namespace lands in the model's namespace, and one naming another namespace is refused. Edit sends the manifest as a replace. Its `resourceVersion` makes the write optimistic, so an edit based on a stale read fails rather than overwriting.

## Example — Pods across namespaces, with logs

```java
@Getter
@Setter
@Erupt(name = "Pods", primaryKeyCol = EruptK8sDataService.QUALIFIED_NAME, power = @Power(add = false, edit = false),
        rowOperation = {
        @RowOperation(code = "logs", title = "Logs", mode = RowOperation.Mode.SINGLE, callHint = "",
                operationParam = "500", operationHandler = K8sPodLogHandler.class)
})
@EruptK8s(apiVersion = "v1", kind = "Pod", namespace = "*")
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class K8sPod {

    @EruptField(views = @View(title = "Pod"))
    private String qualifiedName;

    @EruptField(views = @View(title = "Namespace"), edit = @Edit(title = "Namespace", search = @Search,
            type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = K8sNamespaceFetchHandler.class,
            fetchHandlerParams = "K8sPod")))
    private String namespace;

    @EruptK8sField("status.phase")
    @EruptField(views = @View(title = "Phase"))
    private String phase;

    @EruptK8sField("spec.nodeName")
    @EruptField(views = @View(title = "Node"))
    private String node;
}
```

## Behaviour notes

- **List.** Each list call asks the API server for up to `maxItems` items, with the namespace and label selector applied server-side. Other filters, sorting and paging run in memory.
- **Find by id.** A record is fetched by `name`, or by `namespace/name`. A fixed-namespace model refuses ids that point at another namespace.
- **Scale and restart.** Both send a JSON merge patch on the main resource, so they work on any kind that has `spec.replicas` or `spec.template`, including custom resources.
- **Cluster-scoped kinds.** Nodes, PersistentVolumes and ClusterRoles leave `namespace` empty.
- **Truncation.** `maxItems` truncates silently. Raise it, or narrow by namespace or label selector.
