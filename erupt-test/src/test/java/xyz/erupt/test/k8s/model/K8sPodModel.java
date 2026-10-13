package xyz.erupt.test.k8s.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * Pods across every namespace, keyed by namespace/name; read-only.
 */
@Getter
@Setter
@Erupt(name = "K8s Pod", primaryKeyCol = EruptK8sDataService.QUALIFIED_NAME, authVerify = false)
@EruptK8s(apiVersion = "v1", kind = "Pod", namespace = "*", masterUrl = "${erupt-test.k8s.master-url:}")
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class K8sPodModel {

    @EruptField(views = @View(title = "Pod"))
    private String qualifiedName;

    @EruptField(views = @View(title = "Name"))
    private String name;

    @EruptField(views = @View(title = "Namespace"), edit = @Edit(title = "Namespace", search = @Search))
    private String namespace;

    @EruptK8sField("status.phase")
    @EruptField(views = @View(title = "Phase"))
    private String phase;

}
