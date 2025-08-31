package xyz.erupt.upms.model.online;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * @author YuePeng
 * date 2021/2/16 14:28
 */
@Entity
@Table(name = "e_upms_login_log")
@EruptI18n
@Erupt(
        name = "在线用户",
        filter = @Filter(conditionHandler = EruptOnlineFilterHandler.class),
        power = @Power(add = false, edit = false, viewDetails = false, delete = false, export = true),
        orderBy = "loginTime desc",
        rowOperation = @RowOperation(code = "out", title = "强退", icon = "fa fa-trash-o text-red",
                operationHandler = LogOutOperationHandler.class)
)
@Getter
@Setter
public class EruptOnline extends BaseModel {

    @EruptField(
            views = @View(title = "用户"),
            edit = @Edit(title = "用户", search = @Search(vague = true))
    )
    private String userName;

    @EruptField(
            views = @View(title = "登录时间", sortable = true),
            edit = @Edit(title = "登录时间", search = @Search(vague = true), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date loginTime;

    @EruptField(
            views = @View(title = "IP地址"),
            edit = @Edit(title = "IP地址", search = @Search)
    )
    private String ip;

    @EruptField(
            views = @View(title = "IP来源", desc = "格式：国家 | 大区 | 省份 | 城市 | 运营商", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP来源", search = @Search(vague = true))
    )
    private String region;

    @EruptField(
            views = @View(title = "操作系统"),
            edit = @Edit(title = "操作系统", search = @Search)
    )
    private String systemName;

    @EruptField(
            views = @View(title = "浏览器"),
            edit = @Edit(title = "浏览器", search = @Search)
    )
    private String browser;

    @EruptField(
            views = @View(title = "设备类型"),
            edit = @Edit(title = "设备类型", search = @Search)
    )
    private String deviceType;

    @EruptField(
            edit = @Edit(title = "Token", search = @Search)
    )
    private String token;

}
