package xyz.erupt.k8s_dashboard.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.operation.K8sNamespaceFetchHandler;
import xyz.erupt.k8s.service.EruptK8sClientService;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * @author YuePeng
 */
@Erupt(name = "Config Maps", primaryKeyCol = EruptK8sDataService.QUALIFIED_NAME)
@EruptK8s(apiVersion = "v1", kind = "ConfigMap", masterUrl = KubeCluster.MASTER_URL, kubeConfigPath = KubeCluster.KUBECONFIG, token = KubeCluster.TOKEN,
        namespace = EruptK8sClientService.ALL_NAMESPACES)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class KubeConfigMap {

    @EruptField(views = @View(title = "Name"))
    private String qualifiedName;

    @EruptField(views = @View(title = "Namespace"), edit = @Edit(title = "Namespace", search = @Search,
            type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = K8sNamespaceFetchHandler.class,
            fetchHandlerParams = "KubeConfigMap")))
    private String namespace;

    @EruptField(views = @View(title = "Created", sortable = true))
    private String creationTimestamp;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

    @EruptField(edit = @Edit(title = "YAML", notNull = true, type = EditType.CODE_EDITOR,
            codeEditType = @CodeEditorType(language = "yaml", height = 520)))
    private String yaml;

}
