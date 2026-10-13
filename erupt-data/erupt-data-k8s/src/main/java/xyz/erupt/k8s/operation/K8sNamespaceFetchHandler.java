package xyz.erupt.k8s.operation;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.k8s.service.EruptK8sClientService;

import java.util.ArrayList;
import java.util.List;

/**
 * Namespace dropdown for the search bar of an all-namespaces model; the param names the model whose
 * cluster to read. The chosen namespace is pushed down to the list call.
 * <pre>
 * &#64;EruptField(views = &#64;View(title = "Namespace"), edit = &#64;Edit(title = "Namespace", search = &#64;Search,
 *         type = EditType.CHOICE, choiceType = &#64;ChoiceType(fetchHandler = K8sNamespaceFetchHandler.class,
 *         fetchHandlerParams = "K8sPod")))
 * private String namespace;
 * </pre>
 *
 * @author YuePeng
 */
@Component
public class K8sNamespaceFetchHandler implements ChoiceFetchHandler {

    private final EruptK8sClientService clientService;

    public K8sNamespaceFetchHandler(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) throw new EruptWebApiRuntimeException("K8sNamespaceFetchHandler needs the erupt name as its param");
        EruptModel eruptModel = EruptCoreService.getErupt(params[0]);
        if (null == eruptModel) throw new EruptWebApiRuntimeException("Erupt not found: " + params[0]);
        List<VLModel> namespaces = new ArrayList<>();
        try {
            for (Namespace namespace : clientService.client(clientService.eruptK8s(eruptModel.getClazz())).namespaces().list().getItems()) {
                String name = namespace.getMetadata().getName();
                namespaces.add(new VLModel(name, name));
            }
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
        return namespaces;
    }

}
