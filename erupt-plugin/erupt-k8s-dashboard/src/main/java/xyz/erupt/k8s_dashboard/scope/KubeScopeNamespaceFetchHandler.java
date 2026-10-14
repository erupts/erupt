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

/**
 * Namespaces of the cluster picked in the same form; outside the form, of the current workspace.
 *
 * @author YuePeng
 */
@Component
public class KubeScopeNamespaceFetchHandler implements ChoiceFetchHandler<KubeScope> {

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
