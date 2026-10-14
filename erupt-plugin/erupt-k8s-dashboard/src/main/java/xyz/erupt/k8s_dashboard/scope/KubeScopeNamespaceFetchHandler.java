package xyz.erupt.k8s_dashboard.scope;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.scope.EruptScopes;
import xyz.erupt.k8s.service.EruptK8sClientService;
import xyz.erupt.k8s_dashboard.model.KubeNamespace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Namespaces of the cluster picked in the same form; outside the form, of the current workspace.
 * Every request checks its workspace against these options, so they are kept per cluster for a
 * short while instead of listing the cluster's namespaces each time.
 *
 * @author YuePeng
 */
@Component
public class KubeScopeNamespaceFetchHandler implements ChoiceFetchHandler<KubeScope> {

    // A namespace created or deleted shows up in the picker within this long
    private static final long TTL_MILLIS = 30_000;

    private record Cached(List<VLModel> options, long expireAt) {
    }

    // cluster -> its namespaces; one entry per kubeconfig context, so it stays small
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    private final EruptK8sClientService clientService;

    public KubeScopeNamespaceFetchHandler(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        return this.namespaces(EruptScopes.get(KubeScope.class).getCluster());
    }

    @Override
    public List<VLModel> fetchFilter(KubeScope scope, String[] params) {
        return null == scope || null == scope.getCluster() ? List.of() : this.namespaces(scope.getCluster());
    }

    private List<VLModel> namespaces(String cluster) {
        long now = System.currentTimeMillis();
        Cached cached = cache.get(String.valueOf(cluster));
        if (null != cached && cached.expireAt() > now) return cached.options();
        List<VLModel> options = this.list(cluster);
        cache.put(String.valueOf(cluster), new Cached(options, now + TTL_MILLIS));
        return options;
    }

    private List<VLModel> list(String cluster) {
        List<VLModel> options = new ArrayList<>();
        try {
            for (Namespace namespace : clientService.client(clientService.eruptK8s(KubeNamespace.class), KubeContextFetchHandler.context(cluster))
                    .namespaces().list().getItems()) {
                String name = namespace.getMetadata().getName();
                options.add(new VLModel(name, name));
            }
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
        return options;
    }

}
