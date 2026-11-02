package xyz.erupt.k8s.operation;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.service.EruptK8sClientService;

import java.util.List;

/**
 * {@code kubectl get -o yaml} in a read-only drawer, for models without a YAML field:
 * <pre>
 * &#64;RowOperation(code = "yaml", title = "YAML", mode = RowOperation.Mode.SINGLE,
 *         callHint = "", operationHandler = K8sYamlHandler.class)
 * </pre>
 *
 * @author YuePeng
 */
@Component
public class K8sYamlHandler implements OperationHandler<Object, Void> {

    private final EruptK8sClientService clientService;

    public K8sYamlHandler(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public String exec(List<Object> data, Void form, String[] param) {
        Object row = data.get(0);
        EruptK8s eruptK8s = clientService.eruptK8s(row.getClass());
        EruptK8sClientService.Target target = clientService.target(row);
        try {
            GenericKubernetesResource resource = clientService.resource(eruptK8s, target).get();
            if (null == resource) return null;
            String yaml = clientService.yaml(resource);
            return "codeDrawer('yaml', " + GsonFactory.getGson().toJson(yaml) + ", " + GsonFactory.getGson().toJson(target.id()) + ")";
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
    }

}
