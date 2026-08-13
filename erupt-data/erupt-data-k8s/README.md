# erupt-data-k8s

Kubernetes data source for Erupt, built on the fabric8 kubernetes-client. Bind a `@Erupt` model to a resource kind (Pod, Deployment, Service, ConfigMap, Node, CustomResource, ...) and Erupt gives you a filtered, searchable, permissioned admin view of that resource — with arbitrary field extraction from the resource's nested spec / status.

Read + delete only. Adds and edits are not supported: constructing a K8s spec through a form is fragile compared with authoring YAML.

## Annotations

### `@EruptK8s` (type-level)

| Attribute        | Default | Description                                                                     |
|------------------|---------|---------------------------------------------------------------------------------|
| `apiVersion`     | —       | API group / version, e.g. `v1`, `apps/v1`, `batch/v1`                            |
| `kind`           | —       | Resource kind, e.g. `Pod`, `Deployment`, `ConfigMap`                             |
| `namespace`      | `""`    | Target namespace; empty means cluster-scoped or all namespaces                   |
| `masterUrl`      | `""`    | Master URL override                                                              |
| `kubeConfigPath` | `""`    | Path to a kubeconfig file                                                        |
| `token`          | `""`    | Bearer token override                                                            |
| `maxItems`       | `1000`  | Hard cap on items materialised per list call                                     |

If `kubeConfigPath` / `masterUrl` / `token` are all empty, the client falls back to the standard discovery chain: `KUBECONFIG` env var, `~/.kube/config`, in-cluster service account.

### `@EruptK8sField(path)` (field-level)

Maps a model field to a JSON path inside the resource, e.g. `spec.replicas`, `status.phase`, `spec.template.spec.containers[0].image`. Dot separates object keys and `[n]` indexes into arrays.

### Field shortcuts

These field names resolve automatically without `@EruptK8sField`:

`name`, `namespace`, `uid`, `resourceVersion`, `creationTimestamp`, `labels`, `annotations`, `kind`, `apiVersion`, `metadata`, `spec`, `status`.

## Example — Deployment dashboard

```java
@Getter
@Setter
@Erupt(name = "Deployments", primaryKeyCol = "name")
@EruptK8s(
    apiVersion = "apps/v1",
    kind = "Deployment",
    namespace = "default",
    kubeConfigPath = "/Users/me/.kube/config"
)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class K8sDeployment {

    @EruptField(views = @View(title = "Name"))
    private String name;

    @EruptField(views = @View(title = "Namespace"))
    private String namespace;

    @EruptK8sField("spec.replicas")
    @EruptField(views = @View(title = "Replicas"))
    private Integer replicas;

    @EruptK8sField("status.readyReplicas")
    @EruptField(views = @View(title = "Ready"))
    private Integer readyReplicas;

    @EruptK8sField("spec.template.spec.containers[0].image")
    @EruptField(views = @View(title = "Image"))
    private String image;

    @EruptField(views = @View(title = "Created"))
    private String creationTimestamp;
}
```

## Example — Pod status board

```java
@Getter
@Setter
@Erupt(name = "Pods", primaryKeyCol = "name")
@EruptK8s(apiVersion = "v1", kind = "Pod", namespace = "prod")
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class K8sPod {

    @EruptField(views = @View(title = "Name"))
    private String name;

    @EruptK8sField("status.phase")
    @EruptField(views = @View(title = "Phase"), edit = @Edit(search = @Search))
    private String phase;

    @EruptK8sField("spec.nodeName")
    @EruptField(views = @View(title = "Node"))
    private String node;

    @EruptK8sField("status.containerStatuses[0].restartCount")
    @EruptField(views = @View(title = "Restarts"))
    private Integer restarts;

    @EruptK8sField("status.podIP")
    @EruptField(views = @View(title = "IP"))
    private String podIp;
}
```

## Operations

- **List**: fetches all resources of the kind (bounded by `maxItems`), then filters / sorts / pages in memory.
- **Find by id**: `withName(id).get()` — resource name as the primary key.
- **Delete**: `withName(id).delete()`.
- **Add / edit**: not supported.

## Gotchas

- The primary key value is the resource name; declare the model's primary key column to match a field mapped to `metadata.name` (or a field literally named `name`).
- Cluster-scoped resources (Nodes, PersistentVolumes, ClusterRoles) — leave `namespace` empty.
- `maxItems` truncates silently; raise it or split by namespace / label selector if you regularly hit the cap.
