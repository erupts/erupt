package xyz.erupt.k8s_dashboard.terminal;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.k8s.operation.K8sPodLogHandler;
import xyz.erupt.k8s.service.EruptK8sClientService;

import java.util.*;

/**
 * Binds the terminal page for one Pod row: a ticket per container, in the mode the {@code @Tpl}
 * param names ({@code exec} or {@code logs}).
 * <pre>
 * &#64;RowOperation(code = "exec", title = "Terminal", mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL,
 *         tpl = &#64;Tpl(path = K8sTerminalTplHandler.TPL, params = "exec", tplHandler = K8sTerminalTplHandler.class,
 *                 openWay = OpenWay.DRAWER, drawerPlacement = Placement.BOTTOM, height = "70vh"))
 * </pre>
 *
 * @author YuePeng
 */
@Component
public class K8sTerminalTplHandler implements Tpl.TplHandler {

    public static final String TPL = "/tpl/erupt-k8s-terminal.ftl";

    // FreeMarker binding the operation-tpl endpoint fills with the selected rows
    private static final String ROWS = "rows";

    private final EruptK8sClientService clientService;

    private final K8sTerminalTicketService ticketService;

    public K8sTerminalTplHandler(EruptK8sClientService clientService, K8sTerminalTicketService ticketService) {
        this.clientService = clientService;
        this.ticketService = ticketService;
    }

    @Override
    public void bindTplData(Map<String, Object> binding, String[] params) {
        K8sTerminalTicketService.Mode mode = null != params && params.length > 0 && "logs".equalsIgnoreCase(params[0])
                ? K8sTerminalTicketService.Mode.LOGS : K8sTerminalTicketService.Mode.EXEC;
        Object rows = binding.get(ROWS);
        if (!(rows instanceof List<?> list) || list.isEmpty() || null == list.get(0)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("k8s.primary_key_missing"));
        }
        Object row = list.get(0);
        EruptK8sClientService.Target target = clientService.target(row);
        Pod pod;
        try {
            pod = K8sPodLogHandler.pods(clientService.client(clientService.eruptK8s(row.getClass())), target).get();
        } catch (KubernetesClientException e) {
            throw EruptK8sClientService.wrap(e);
        }
        if (null == pod) throw new EruptWebApiRuntimeException("Pod not found: " + target.id());
        String token = MetaContext.getToken();
        String namespace = pod.getMetadata().getNamespace();
        List<Map<String, String>> containers = new ArrayList<>();
        for (Container container : pod.getSpec().getContainers()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", container.getName());
            item.put("ticket", ticketService.issue(token, row.getClass(), namespace, target.name(), container.getName(), mode));
            containers.add(item);
        }
        binding.put("title", target.id());
        binding.put("mode", mode.name().toLowerCase(Locale.ROOT));
        binding.put("containers", containers);
    }

}
