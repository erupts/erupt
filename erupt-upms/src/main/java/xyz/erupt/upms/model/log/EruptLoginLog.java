package xyz.erupt.upms.model.log;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.cube.Cube;
import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.Measure;
import xyz.erupt.annotation.cube.SqlType;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Date;

/**
 * @author YuePeng
 * date 2019-11-27.
 */
@Entity
@Table(name = "e_upms_login_log")
@Erupt(
        name = "Login Log",
        power = @Power(add = false, edit = false, viewDetails = false, delete = false,
                export = true, powerHandler = SuperAdminPower.class),
        orderBy = "loginTime desc"
)
@Cube(
        name = "Erupt Login Log",
        sql = "e_upms_login_log",
        sqlType = SqlType.TABLE_NAME
)
@EruptI18n
@Getter
@Setter
public class EruptLoginLog extends BaseModel {

    @Dimension(title = "User Name", sql = "user_name")
    @EruptField(
            views = @View(title = "Account"),
            edit = @Edit(title = "Account", search = @Search(vague = true))
    )
    private String userName;

    @Dimension(title = "Login Time", sql = "login_time")
    @EruptField(
            views = @View(title = "Login Time", sortable = true),
            edit = @Edit(title = "Login Time", search = @Search(vague = true), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date loginTime;

    @Dimension(title = "IP Address", sql = "ip")
    @Column(length = 64)
    @EruptField(
            views = @View(title = "IP Address"),
            edit = @Edit(title = "IP Address", search = @Search)
    )
    private String ip;

    @Dimension(title = "IP Region", sql = "region")
    @EruptField(
            views = @View(title = "IP Source", desc = "国家 | 大区 | 省份 | 城市 | 运营商", width = "250px", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP Source", search = @Search(vague = true))
    )
    private String region;

    @Dimension(title = "System Name", sql = "system_name")
    @EruptField(
            views = @View(title = "OS"),
            edit = @Edit(title = "OS", search = @Search)
    )
    private String systemName;

    @Dimension(title = "Browser", sql = "browser")
    @EruptField(
            views = @View(title = "Browser"),
            edit = @Edit(title = "Browser", search = @Search)
    )
    private String browser;

    @Dimension(title = "Device Type", sql = "device_type")
    @EruptField(
            views = @View(title = "Device Type"),
            edit = @Edit(title = "Device Type", search = @Search)
    )
    private String deviceType;

    private String token;

    @Transient
    @Measure(title = "Count", sql = "count(*)")
    private String count;

}
