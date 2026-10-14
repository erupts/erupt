package xyz.erupt.upms.model.online;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;

import java.util.Date;

/**
 * @author YuePeng
 * date 2021/2/16 14:28
 */
@Entity
@Table(name = "e_upms_login_log")
@EruptI18n
@Erupt(
        name = "Online Users",
        filter = @Filter(conditionHandler = EruptOnlineFilterHandler.class),
        power = @Power(add = false, edit = false, viewDetails = false, delete = false, export = true, print = false),
        orderBy = "loginTime desc",
        rowOperation = @RowOperation(code = "out", title = "Force Logout", icon = "fa fa-trash-o text-red",
                operationHandler = LogOutOperationHandler.class)
)
@Getter
@Setter
public class EruptOnline extends BaseModel {

    // same table as EruptLoginLog: the tenant restriction has to apply to this view as well,
    // so a tenant only lists and logs out its own sessions
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @EruptField(
            views = @View(title = "Account"),
            edit = @Edit(title = "Account", search = @Search(operator = QueryExpression.LIKE))
    )
    private String userName;

    @EruptField(
            views = @View(title = "Login Time", sortable = true),
            edit = @Edit(title = "Login Time", search = @Search, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date loginTime;

    @EruptField(
            views = @View(title = "IP Address"),
            edit = @Edit(title = "IP Address", search = @Search)
    )
    private String ip;

    @EruptField(
            views = @View(title = "IP Source", desc = "Country | Province | City | ISP | Country Code", width = "300px", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP Source", search = @Search)
    )
    private String region;

    @EruptField(
            views = @View(title = "OS"),
            edit = @Edit(title = "OS", search = @Search)
    )
    private String systemName;

    @EruptField(
            views = @View(title = "Browser"),
            edit = @Edit(title = "Browser", search = @Search)
    )
    private String browser;

    @EruptField(
            views = @View(title = "Device Type"),
            edit = @Edit(title = "Device Type", search = @Search)
    )
    private String deviceType;

    @EruptField(
            edit = @Edit(title = "Token", search = @Search)
    )
    private String token;

}
