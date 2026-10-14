package xyz.erupt.k8s_dashboard.scope;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;

/**
 * The workspace every dashboard model follows: the cluster to talk to and, optionally, one
 * namespace. Picked once in the page header.
 *
 * @author YuePeng
 */
@Getter
@Setter
// A form, not a table: its first field doubles as the key erupt requires
@Erupt(name = "Workspace", primaryKeyCol = "cluster")
public class KubeScope {

    @EruptField(edit = @Edit(title = "Cluster", type = EditType.CHOICE, notNull = true,
            choiceType = @ChoiceType(fetchHandler = KubeContextFetchHandler.class)))
    private String cluster;

    // Left empty: every namespace
    @EruptField(edit = @Edit(title = "Namespace", type = EditType.CHOICE,
            choiceType = @ChoiceType(fetchHandler = KubeScopeNamespaceFetchHandler.class, dependField = "cluster")))
    private String namespace;

}
