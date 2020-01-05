package xyz.erupt.auth.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-11-27.
 */
@Entity
@Table(name = "E_USER_LOGIN_LOG")
@Erupt(
        name = "登录日志",
        power = @Power(add = false, edit = false, viewDetails = false, delete = false, export = true),
        orderBy = "loginTime desc"
)
@Getter
@Setter
public class EruptLoginLog extends BaseModel {

    @ManyToOne
    @EruptField(
            views = @View(title = "用户名", column = "name"),
            edit = @Edit(title = "用户名", type = EditType.REFERENCE_TABLE, search = @Search(vague = true, value = true))
    )
    private EruptUser eruptUser;

    @EruptField(
            views = @View(title = "登录时间", sortable = true),
            edit = @Edit(title = "登录时间", search = @Search(vague = true, value = true))
    )
    private Date loginTime;

    @EruptField(
            views = @View(title = "IP地址"),
            edit = @Edit(title = "IP地址", search = @Search(true))
    )
    private String ip;

    @EruptField(
            views = @View(title = "操作系统"),
            edit = @Edit(title = "操作系统", search = @Search(true))
    )
    private String system;

    @EruptField(
            views = @View(title = "浏览器"),
            edit = @Edit(title = "浏览器", search = @Search(true))
    )
    private String browser;

    @EruptField(
            views = @View(title = "设备类型"),
            edit = @Edit(title = "设备类型", search = @Search(true))
    )
    private String deviceType;
}
