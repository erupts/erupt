package xyz.erupt.k8s_dashboard.scope;

import org.springframework.stereotype.Component;
import xyz.erupt.core.scope.EruptScopes;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.service.K8sContextResolver;
import xyz.erupt.k8s_dashboard.model.KubeCluster;

/**
 * Points every dashboard model at the cluster of the current workspace. Models of the application's
 * own are left alone: they are told apart by the dashboard's connection placeholder.
 *
 * @author YuePeng
 */
@Component
public class KubeScopeContextResolver implements K8sContextResolver {

    @Override
    public String context(EruptK8s eruptK8s) {
        if (!KubeCluster.MASTER_URL.equals(eruptK8s.masterUrl())) return null;
        return KubeContextFetchHandler.context(EruptScopes.get(KubeScope.class).getCluster());
    }

}
