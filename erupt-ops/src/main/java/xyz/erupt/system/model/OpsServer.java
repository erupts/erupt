package xyz.erupt.system.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
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
@Table(name = "E_OPS_SERVER"
//    uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
@Erupt(
        name = "服务器管理",
        orderBy = "sort",
        linkTree = @LinkTree(field = "opsServerGroup", dependNode = true)
)
public class OpsServer extends HyperModel {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "地址"),
            edit = @Edit(title = "地址", notNull = true)
    )
    private String address;

    @EruptField(
            views = @View(title = "端口"),
            edit = @Edit(title = "端口", notNull = true)
    )
    private Integer port;

    @ManyToOne
    @EruptField(
            views = @View(title = "所属组别", column = "name"),
            edit = @Edit(title = "所属组别", type = EditType.REFERENCE_TREE, show = false,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"))
    )
    private OpsServerGroup opsServerGroup;

    @EruptField(
            views = @View(title = "用户名"),
            edit = @Edit(title = "用户名", notNull = true)
    )
    private String username;

    @EruptField(
            edit = @Edit(title = "密码", inputType = @InputType(type = "password"))
    )
    private String password;

    @EruptField(
            views = @View(title = "显示顺序"),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @Lob
    @EruptField(
            views = @View(title = "备注", type = ViewType.HTML),
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;


}
