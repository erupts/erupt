package xyz.erupt.k8s.operation;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.k8s.service.EruptK8sClientService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * {@code kubectl rollout restart} for Deployment, StatefulSet and DaemonSet: stamps the pod template
 * so the controller rolls every pod.
 * <pre>
 * &#64;RowOperation(code = "restart", title = "Restart", operationHandler = K8sRestartHandler.class)
 * </pre>
 *
 * @author YuePeng
 */
@Component
public class K8sRestartHandler implements OperationHandler<Object, Void> {

    // The annotation kubectl itself writes, so kubectl rollout history reads the same way
    private static final String RESTARTED_AT = "kubectl.kubernetes.io/restartedAt";

    private final EruptK8sClientService clientService;

    public K8sRestartHandler(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public String exec(List<Object> data, Void form, String[] param) {
        String now = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String patch = "{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"" + RESTARTED_AT + "\":\"" + now + "\"}}}}}";
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

}
