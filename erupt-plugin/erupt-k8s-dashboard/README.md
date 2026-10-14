# erupt-k8s-dashboard

A ready-made Kubernetes admin. Add the dependency and a **Kubernetes** menu appears with the common resources, already wired with logs, a web terminal, scaling, restarts and YAML editing. You write no models and no annotations.

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-k8s-dashboard</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

Built on [erupt-data-k8s](../../erupt-data/erupt-data-k8s). To model a resource the dashboard does not ship, such as a custom resource, use `@EruptK8s` there.

## What you get

| Menu | Kind | Actions |
|---|---|---|
| Pods | Pod | Logs, Follow logs, Terminal, YAML, delete |
| Deployments | Deployment | Scale, Restart, create / edit from YAML, delete |
| Stateful Sets | StatefulSet | Scale, Restart, create / edit from YAML, delete |
| Daemon Sets | DaemonSet | Restart, create / edit from YAML, delete |
| Services | Service | create / edit from YAML, delete |
| Config Maps | ConfigMap | create / edit from YAML, delete |
| Events | Event | read only |
| Namespaces | Namespace | read only, YAML |
| Nodes | Node | read only, YAML |

Namespaced lists follow the Workspace namespace, or span every namespace when none is picked; a label selector narrows them further. Both are applied by the API server.

## Which cluster: the Workspace

The header shows a **Workspace** picker: a cluster and, optionally, one namespace. Every Kubernetes page follows it. Each browser window keeps its own Workspace, so two windows can look at two clusters. Switching refreshes the window's open tabs, so none keeps acting on the cluster it was opened with; a new window starts from the last pick.

- **Cluster** lists the contexts of the kubeconfig the dashboard reads, current context first. That context is the default, so `kubectl config use-context` is no longer needed to look at another cluster.
- **Namespace** lists that cluster's namespaces. Leave it empty to see every namespace. A picked namespace filters the namespaced pages, hides their namespace search and column, and new resources land in it.

The pick travels with every request as plain JSON (`erupt-scope` header), and the server checks it against the options the user is offered, so a cluster or namespace they were not offered is ignored. A passed check holds for a minute. The Workspace is erupt's `@EruptScope` feature; `KubeScope` is a plain erupt form.

The kubeconfig comes from the standard client chain: `KUBECONFIG`, then `~/.kube/config`, then the in-cluster service account when erupt runs inside Kubernetes. To read another file, or to pin one cluster:

```yaml
erupt:
  k8s-dashboard:
    kubeconfig: /etc/erupt/kubeconfig   # its contexts become the Workspace clusters, or:
    master-url: https://10.0.0.1:6443   # a single cluster, no switching
    token: ${K8S_TOKEN}                 # keep it in the environment, never in the file
```

Setting `master-url` without `kubeconfig` ignores `~/.kube/config`. The dashboard can do no more than its credentials allow, so give it a service account scoped to what your admins should touch.

## Permissions

Each resource is its own menu, so roles grant them one by one. Add, edit and delete follow the menu's buttons as with any erupt table.

**The terminal is a separate grant.** The Pods menu has a **Terminal** button (`KubePod@TERMINAL`). Users who see pods but lack this button get no Terminal action, and the server refuses to render the terminal page for them. Logs stay with the Pods menu.

## Web terminal

The terminal opens in a bottom drawer. **Terminal** runs `bash`, or `sh` when the image has no bash. **Follow logs** streams the last 500 lines and then follows. A pod with several containers gets a container switcher.

- **Socket path.** The socket listens on `/erupt-k8s-terminal`. A reverse proxy in front of erupt must pass WebSocket upgrades on that path.
- **Tickets.** The page carries one-time tickets that expire after 60 seconds. Each ticket is bound to the user's session, the pod and the container, and the socket never takes a pod name from the browser. Reopen the action to reconnect.
- **Offline.** xterm.js is bundled in the jar under `/erupt-k8s-dashboard/xterm/`, so the terminal also works without internet access.

## Notes

- **Volume.** Each list call reads up to 1000 items and pages them in memory. Narrow large clusters by namespace.
- **Events** across every namespace are capped like any list; pick a namespace in the Workspace on busy clusters.
- **Writes.** Editing from YAML is a replace with the manifest's `resourceVersion`. An edit based on a stale read fails instead of overwriting.
