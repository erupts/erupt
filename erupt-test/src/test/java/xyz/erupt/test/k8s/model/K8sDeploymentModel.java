package xyz.erupt.test.k8s.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.operation.K8sRestartHandler;
import xyz.erupt.k8s.operation.K8sScaleForm;
import xyz.erupt.k8s.operation.K8sScaleHandler;
import xyz.erupt.k8s.operation.K8sYamlHandler;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * Deployments of one namespace, writable through the yaml field.
 */
@Getter
@Setter
@Erupt(name = "K8s Deployment", primaryKeyCol = "name", authVerify = false, rowOperation = {
        @RowOperation(code = "scale", title = "Scale", mode = RowOperation.Mode.SINGLE,
                eruptClass = K8sScaleForm.class, operationHandler = K8sScaleHandler.class),
        @RowOperation(code = "restart", title = "Restart", operationHandler = K8sRestartHandler.class),
        @RowOperation(code = "yaml", title = "YAML", mode = RowOperation.Mode.SINGLE, callHint = "",
                operationHandler = K8sYamlHandler.class)
})
@EruptK8s(apiVersion = "apps/v1", kind = "Deployment", namespace = "demo", masterUrl = "${erupt-test.k8s.master-url:}")
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class K8sDeploymentModel {

    @EruptField(views = @View(title = "Name"))
    private String name;

    @EruptField(views = @View(title = "Namespace"))
    private String namespace;

    @EruptK8sField("spec.replicas")
    @EruptField(views = @View(title = "Replicas"))
    private Integer replicas;

    @EruptField(edit = @Edit(title = "Label selector", search = @Search))
    private String labelSelector;

    @EruptField(edit = @Edit(title = "YAML", notNull = true, type = EditType.CODE_EDITOR,
            codeEditType = @CodeEditorType(language = "yaml")))
    private String yaml;

}
