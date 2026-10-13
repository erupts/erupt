package xyz.erupt.k8s_dashboard.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.operation.K8sYamlHandler;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * @author YuePeng
 */
@Erupt(name = "Nodes", primaryKeyCol = "name", power = @Power(add = false, edit = false, delete = false, viewDetails = false),
        rowOperation = @RowOperation(code = "yaml", title = "YAML", icon = "fa fa-code", mode = RowOperation.Mode.SINGLE,
                callHint = "", operationHandler = K8sYamlHandler.class))
@EruptK8s(apiVersion = "v1", kind = "Node", masterUrl = KubeCluster.MASTER_URL, kubeConfigPath = KubeCluster.KUBECONFIG, token = KubeCluster.TOKEN)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class KubeNode {

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", search = @Search))
    private String name;

    @EruptK8sField("status.addresses[0].address")
    @EruptField(views = @View(title = "Address"))
    private String address;

    @EruptK8sField("status.nodeInfo.kubeletVersion")
    @EruptField(views = @View(title = "Version"))
    private String version;

    @EruptK8sField("status.nodeInfo.osImage")
    @EruptField(views = @View(title = "OS image"))
    private String osImage;

    @EruptK8sField("status.nodeInfo.architecture")
    @EruptField(views = @View(title = "Architecture"))
    private String architecture;

    @EruptK8sField("status.capacity.cpu")
    @EruptField(views = @View(title = "CPU"))
    private String cpu;

    @EruptK8sField("status.capacity.memory")
    @EruptField(views = @View(title = "Memory"))
    private String memory;

    @EruptK8sField("spec.unschedulable")
    @EruptField(views = @View(title = "Unschedulable"))
    private Boolean unschedulable;

    @EruptField(views = @View(title = "Created", sortable = true))
    private String creationTimestamp;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

}
