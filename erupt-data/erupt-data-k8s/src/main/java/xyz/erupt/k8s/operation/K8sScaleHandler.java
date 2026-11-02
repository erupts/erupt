package xyz.erupt.k8s.operation;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.k8s.service.EruptK8sClientService;

import java.util.List;
import java.util.Map;

/**
 * {@code kubectl scale} for workloads with {@code spec.replicas} (Deployment, StatefulSet, ReplicaSet,
 * and custom resources that follow the convention):
 * <pre>
 * &#64;RowOperation(code = "scale", title = "Scale", mode = RowOperation.Mode.SINGLE,
 *         eruptClass = K8sScaleForm.class, operationHandler = K8sScaleHandler.class)
 * </pre>
 *
 * @author YuePeng
 */
@Component
public class K8sScaleHandler implements OperationHandler<Object, K8sScaleForm> {

    private final EruptK8sClientService clientService;

    public K8sScaleHandler(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public String exec(List<Object> data, K8sScaleForm form, String[] param) {
        if (null == form.getReplicas() || form.getReplicas() < 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.replicas_invalid"));
        }
        // A merge patch on spec.replicas works for any kind with the field, scale subresource or not
        String patch = "{\"spec\":{\"replicas\":" + form.getReplicas() + "}}";
        for (Object row : data) {
            try {
                clientService.resource(clientService.eruptK8s(row.getClass()), clientService.target(row))
                        .patch(PatchContext.of(PatchType.JSON_MERGE), patch);
            } catch (KubernetesClientException e) {
                throw EruptK8sClientService.wrap(e);
            }
        }
        return null;
    }

    // Opens with the current replica count
    @Override
    public K8sScaleForm eruptFormValue(List<Object> data, K8sScaleForm form, String[] param) {
        if (null == data || data.isEmpty()) return form;
        Object row = data.get(0);
        try {
            GenericKubernetesResource resource = clientService.resource(clientService.eruptK8s(row.getClass()), clientService.target(row)).get();
            if (null != resource && resource.getAdditionalProperties().get("spec") instanceof Map<?, ?> spec
                    && spec.get("replicas") instanceof Number replicas) {
                form.setReplicas(replicas.intValue());
            }
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
        return form;
    }

}
