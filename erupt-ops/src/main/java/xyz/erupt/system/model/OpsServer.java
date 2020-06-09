package xyz.erupt.system.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.auth.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author liyuepeng
 * @date 12/7/18.
 */

@Entity
@Table(name = "E_OPS_SERVER", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "服务器管理",
        orderBy = "sort"
)
public class OpsServer extends BaseModel {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "IP"),
            edit = @Edit(title = "IP", notNull = true)
    )
    private String ip;

    @EruptField(
            views = @View(title = "公网IP"),
            edit = @Edit(title = "公网IP")
    )
    private String publicIp;

    @EruptField(
            views = @View(title = "端口"),
            edit = @Edit(title = "端口", notNull = true)
    )
    private Integer port;

    @EruptField(
            views = @View(title = "账号"),
            edit = @Edit(title = "账号", notNull = true)
    )
    private String username;

    @EruptField(
            views = @View(title = "密码"),
            edit = @Edit(title = "密码")
    )
    private String password;

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
