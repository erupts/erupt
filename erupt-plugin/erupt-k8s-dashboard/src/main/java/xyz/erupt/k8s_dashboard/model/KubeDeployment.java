package xyz.erupt.k8s_dashboard.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.operation.K8sNamespaceFetchHandler;
import xyz.erupt.k8s.operation.K8sRestartHandler;
import xyz.erupt.k8s.operation.K8sScaleForm;
import xyz.erupt.k8s.operation.K8sScaleHandler;
import xyz.erupt.k8s_dashboard.scope.KubeScope;
import xyz.erupt.k8s.service.EruptK8sClientService;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * @author YuePeng
 */
@Erupt(name = "Deployments", primaryKeyCol = EruptK8sDataService.QUALIFIED_NAME, rowOperation = {
        @RowOperation(code = "scale", title = "Scale", icon = "fa fa-up-right-and-down-left-from-center", mode = RowOperation.Mode.SINGLE,
                eruptClass = K8sScaleForm.class, operationHandler = K8sScaleHandler.class),
        @RowOperation(code = "restart", title = "Restart", icon = "fa fa-rotate-right", operationHandler = K8sRestartHandler.class)
})
@EruptScope(KubeScope.class)
@EruptK8s(apiVersion = "apps/v1", kind = "Deployment", masterUrl = KubeCluster.MASTER_URL, kubeConfigPath = KubeCluster.KUBECONFIG, token = KubeCluster.TOKEN,
        namespace = EruptK8sClientService.ALL_NAMESPACES)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class KubeDeployment {

    @EruptField(views = @View(title = "Name"))
    private String qualifiedName;

    @EruptField(views = @View(title = "Namespace"), edit = @Edit(title = "Namespace", search = @Search,
            type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = K8sNamespaceFetchHandler.class,
            fetchHandlerParams = "KubeDeployment")))
    private String namespace;

    @EruptK8sField("spec.replicas")
    @EruptField(views = @View(title = "Replicas", sortable = true))
    private Integer replicas;

    @EruptK8sField("status.readyReplicas")
    @EruptField(views = @View(title = "Ready"))
    private Integer readyReplicas;

    @EruptK8sField("status.availableReplicas")
    @EruptField(views = @View(title = "Available"))
    private Integer availableReplicas;

    @EruptK8sField("spec.template.spec.containers[0].image")
    @EruptField(views = @View(title = "Image"))
    private String image;

    @EruptField(views = @View(title = "Created", sortable = true))
    private String creationTimestamp;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

    @EruptField(edit = @Edit(title = "YAML", notNull = true, type = EditType.CODE_EDITOR,
            codeEditType = @CodeEditorType(language = "yaml", height = 520)))
    private String yaml;

}
