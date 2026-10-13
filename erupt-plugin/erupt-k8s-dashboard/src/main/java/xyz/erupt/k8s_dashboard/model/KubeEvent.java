package xyz.erupt.k8s_dashboard.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.k8s.annotation.EruptK8s;
import xyz.erupt.k8s.annotation.EruptK8sField;
import xyz.erupt.k8s.operation.K8sNamespaceFetchHandler;
import xyz.erupt.k8s.service.EruptK8sClientService;
import xyz.erupt.k8s.service.EruptK8sDataService;

/**
 * Busy clusters keep thousands of events, so the list waits for a namespace instead of pulling them all.
 *
 * @author YuePeng
 */
@Erupt(name = "Events", primaryKeyCol = EruptK8sDataService.QUALIFIED_NAME, orderBy = "lastTimestamp desc",
        power = @Power(add = false, edit = false, delete = false, viewDetails = false))
@EruptK8s(apiVersion = "v1", kind = "Event", masterUrl = KubeCluster.MASTER_URL, kubeConfigPath = KubeCluster.KUBECONFIG, token = KubeCluster.TOKEN,
        namespace = EruptK8sClientService.ALL_NAMESPACES)
@EruptDataProcessor(EruptK8sDataService.DATA_PROCESSOR)
public class KubeEvent {

    @EruptField
    private String qualifiedName;

    @EruptField(views = @View(title = "Namespace"), edit = @Edit(title = "Namespace", search = @Search(notNull = true),
            type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = K8sNamespaceFetchHandler.class,
            fetchHandlerParams = "KubeEvent")))
    private String namespace;

    @EruptK8sField("type")
    @EruptField(views = @View(title = "Type"), edit = @Edit(title = "Type", search = @Search))
    private String type;

    @EruptK8sField("reason")
    @EruptField(views = @View(title = "Reason"), edit = @Edit(title = "Reason", search = @Search))
    private String reason;

    @EruptK8sField("involvedObject.kind")
    @EruptField(views = @View(title = "Kind"))
    private String objectKind;

    @EruptK8sField("involvedObject.name")
    @EruptField(views = @View(title = "Object"), edit = @Edit(title = "Object", search = @Search))
    private String objectName;

    @EruptK8sField("message")
    @EruptField(views = @View(title = "Message"))
    private String message;

    @EruptK8sField("count")
    @EruptField(views = @View(title = "Count"))
    private Integer count;

    @EruptK8sField("lastTimestamp")
    @EruptField(views = @View(title = "Last seen", sortable = true))
    private String lastTimestamp;

}
