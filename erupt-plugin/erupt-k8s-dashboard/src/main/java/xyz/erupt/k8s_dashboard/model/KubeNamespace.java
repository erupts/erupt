package xyz.erupt.k8s_dashboard.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.operation.K8sYamlHandler;
import xyz.erupt.k8s_dashboard.scope.KubeScope;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * Read-only: deleting a namespace takes everything in it along.
 *
 * @author YuePeng
 */
@Erupt(name = "Namespaces", primaryKeyCol = "name", power = @Power(add = false, edit = false, delete = false, viewDetails = false),
        rowOperation = @RowOperation(code = "yaml", title = "YAML", icon = "fa fa-code", mode = RowOperation.Mode.SINGLE,
                callHint = "", operationHandler = K8sYamlHandler.class))
@EruptScope(KubeScope.class)
@EruptK8s(apiVersion = "v1", kind = "Namespace", masterUrl = KubeCluster.MASTER_URL, kubeConfigPath = KubeCluster.KUBECONFIG, token = KubeCluster.TOKEN)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class KubeNamespace {

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", search = @Search))
    private String name;

    @EruptK8sField("status.phase")
    @EruptField(views = @View(title = "Status"))
    private String phase;

    @EruptField(views = @View(title = "Created", sortable = true))
    private String creationTimestamp;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

}
