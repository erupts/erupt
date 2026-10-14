package xyz.erupt.k8s_dashboard.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.operation.K8sNamespaceFetchHandler;
import xyz.erupt.k8s.operation.K8sPodLogHandler;
import xyz.erupt.k8s.operation.K8sYamlHandler;
import xyz.erupt.k8s_dashboard.scope.KubeScope;
import xyz.erupt.k8s.service.EruptK8sClientService;
import xyz.erupt.k8s.service.EruptK8sDataService;
import xyz.erupt.k8s_dashboard.terminal.K8sTerminalTplHandler;
import xyz.erupt.upms.handler.ViaMenuValueCtrl;

/**
 * @author YuePeng
 */
@Erupt(name = "Pods", primaryKeyCol = EruptK8sDataService.QUALIFIED_NAME, power = @Power(add = false, edit = false),
        rowOperation = {
                @RowOperation(code = "logs", title = "Logs", icon = "fa fa-file-lines", mode = RowOperation.Mode.SINGLE,
                        callHint = "", operationParam = "500", operationHandler = K8sPodLogHandler.class),
                @RowOperation(code = "follow", title = "Follow logs", icon = "fa fa-scroll", mode = RowOperation.Mode.SINGLE,
                        type = RowOperation.Type.TPL, tpl = @Tpl(path = K8sTerminalTplHandler.TPL, params = "logs",
                        tplHandler = K8sTerminalTplHandler.class, openWay = OpenWay.DRAWER, drawerPlacement = Placement.BOTTOM, height = "70vh")),
                @RowOperation(code = "exec", title = "Terminal", icon = "fa fa-terminal", mode = RowOperation.Mode.SINGLE,
                        show = @ExprBool(exprHandler = ViaMenuValueCtrl.class, params = KubePod.TERMINAL),
                        type = RowOperation.Type.TPL, tpl = @Tpl(path = K8sTerminalTplHandler.TPL, params = "exec",
                        tplHandler = K8sTerminalTplHandler.class, openWay = OpenWay.DRAWER, drawerPlacement = Placement.BOTTOM, height = "70vh")),
                @RowOperation(code = "yaml", title = "YAML", icon = "fa fa-code", mode = RowOperation.Mode.SINGLE,
                        callHint = "", operationHandler = K8sYamlHandler.class)
        })
@EruptScope(KubeScope.class)
@EruptK8s(apiVersion = "v1", kind = "Pod", masterUrl = KubeCluster.MASTER_URL, kubeConfigPath = KubeCluster.KUBECONFIG, token = KubeCluster.TOKEN,
        namespace = EruptK8sClientService.ALL_NAMESPACES)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class KubePod {

    // Menu value of the button that grants the web terminal
    public static final String TERMINAL = "KubePod@TERMINAL";

    @EruptField(views = @View(title = "Name"))
    private String qualifiedName;

    @EruptField(views = @View(title = "Namespace"), edit = @Edit(title = "Namespace", search = @Search,
            type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = K8sNamespaceFetchHandler.class,
            fetchHandlerParams = "KubePod")))
    private String namespace;

    // Exact choices, so the search runs on the API server as a field selector and the list stays paged
    @EruptK8sField(value = "status.phase", fieldSelector = true)
    @EruptField(views = @View(title = "Status"), edit = @Edit(title = "Status", search = @Search, type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {@VL(value = "Pending", label = "Pending"), @VL(value = "Running", label = "Running"),
                    @VL(value = "Succeeded", label = "Succeeded"), @VL(value = "Failed", label = "Failed"),
                    @VL(value = "Unknown", label = "Unknown")})))
    private String phase;

    @EruptK8sField("status.podIP")
    @EruptField(views = @View(title = "Pod IP"))
    private String podIp;

    // Typed, fuzzy by default (filtered in memory); switch the operator to equals and the API server
    // selects the exact node itself, keeping the list paged
    @EruptK8sField(value = "spec.nodeName", fieldSelector = true)
    @EruptField(views = @View(title = "Node"), edit = @Edit(title = "Node", search = @Search(operator = QueryExpression.LIKE)))
    private String node;

    @EruptK8sField("spec.containers[0].image")
    @EruptField(views = @View(title = "Image"))
    private String image;

    @EruptK8sField("status.containerStatuses[0].restartCount")
    @EruptField(views = @View(title = "Restarts", sortable = true))
    private Integer restarts;

    @EruptField(views = @View(title = "Created", sortable = true))
    private String creationTimestamp;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

}
