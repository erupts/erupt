package xyz.erupt.system.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.auth.model.base.HyperModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 12/7/18.
 */

@Entity
@Table(name = "E_OPS_SERVER_GROUP")
@Erupt(
        name = "服务器组别",
        orderBy = "OpsServerGroup.sort",
        tree = @Tree(pid = "parent.id")
)
public class OpsServerGroup extends HyperModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @ManyToOne
    @EruptField(
            edit = @Edit(
                    title = "上级组别",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id")
            )
    )
    private OpsServerGroup parent;

    @EruptField(
            views = @View(title = "显示顺序"),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @Lob
    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;


}
