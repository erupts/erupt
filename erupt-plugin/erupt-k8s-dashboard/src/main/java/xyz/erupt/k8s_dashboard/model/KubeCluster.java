package xyz.erupt.k8s_dashboard.model;

/**
 * The cluster every dashboard model talks to. All three are empty by default, which selects the
 * standard client chain: {@code KUBECONFIG}, {@code ~/.kube/config}, then the in-cluster service account.
 * <pre>
 * erupt:
 *   k8s-dashboard:
 *     kubeconfig: /etc/erupt/kubeconfig   # or master-url + token
 * </pre>
 *
 * @author YuePeng
 */
public final class KubeCluster {

    public static final String MASTER_URL = "${erupt.k8s-dashboard.master-url:}";

    public static final String KUBECONFIG = "${erupt.k8s-dashboard.kubeconfig:}";

    public static final String TOKEN = "${erupt.k8s-dashboard.token:}";

    private KubeCluster() {
    }

}
