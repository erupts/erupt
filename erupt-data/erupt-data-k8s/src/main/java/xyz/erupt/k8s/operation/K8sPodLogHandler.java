package xyz.erupt.k8s.operation;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.k8s.service.EruptK8sClientService;

import java.util.List;

/**
 * {@code kubectl logs --tail} for a Pod model, every container in one drawer. The optional first
 * operation param sets the line count:
 * <pre>
 * &#64;RowOperation(code = "logs", title = "Logs", mode = RowOperation.Mode.SINGLE, callHint = "",
 *         operationParam = "500", operationHandler = K8sPodLogHandler.class)
 * </pre>
 * For a live stream use the web terminal in logs mode instead.
 *
 * @author YuePeng
 */
@Component
public class K8sPodLogHandler implements OperationHandler<Object, Void> {

    private static final int DEFAULT_TAIL = 500;

    private static final int MAX_TAIL = 10000;

    private final EruptK8sClientService clientService;

    public K8sPodLogHandler(EruptK8sClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public String exec(List<Object> data, Void form, String[] param) {
        Object row = data.get(0);
        int tail = DEFAULT_TAIL;
        if (null != param && param.length > 0) {
            try {
                tail = Math.min(Math.max(Integer.parseInt(param[0].trim()), 1), MAX_TAIL);
            } catch (NumberFormatException ignore) {
                // keep the default
            }
        }
        EruptK8sClientService.Target target = clientService.target(row);
        KubernetesClient client = clientService.client(clientService.eruptK8s(row.getClass()));
        PodResource pod = pods(client, target);
        StringBuilder logs = new StringBuilder();
        try {
            Pod current = pod.get();
            if (null == current) throw new EruptWebApiRuntimeException("Pod not found: " + target.id());
            List<Container> containers = current.getSpec().getContainers();
            for (Container container : containers) {
                if (containers.size() > 1) logs.append("==> ").append(container.getName()).append(" <==\n");
                logs.append(pod.inContainer(container.getName()).tailingLines(tail).getLog()).append('\n');
            }
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
        return "codeDrawer('text', " + GsonFactory.getGson().toJson(logs.toString()) + ", " + GsonFactory.getGson().toJson(target.id()) + ")";
    }

    public static PodResource pods(KubernetesClient client, EruptK8sClientService.Target target) {
        return null == target.namespace() ? client.pods().withName(target.name())
                : client.pods().inNamespace(target.namespace()).withName(target.name());
    }

}
