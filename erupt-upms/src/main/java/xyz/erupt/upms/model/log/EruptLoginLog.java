package xyz.erupt.upms.model.log;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.EruptCube;
import xyz.erupt.annotation.cube.Measure;
import xyz.erupt.annotation.cube.SqlType;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;

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
@EruptCube(
        name = "Erupt Login Log",
        // cubes read the table with plain SQL, past Hibernate's tenant filter: restrict it here
        sql = "select * from e_upms_login_log where tenant_id = '#if($user.tenantId)$user.tenantId#{else}" + UpmsTenant.PLATFORM + "#end'",
        sqlType = SqlType.SUB_QUERY
)
@EruptI18n
@Getter
@Setter
public class EruptLoginLog extends BaseModel {

    // Owner tenant, UpmsTenant.PLATFORM for the platform, restricted and stamped by Hibernate
    // (see UpmsTenantIdentifierResolver); EruptOnline maps the same table and carries it too
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @Dimension(title = "User Name", sql = "user_name")
    @EruptField(
            views = @View(title = "Account"),
            edit = @Edit(title = "Account", search = @Search(operator = QueryExpression.LIKE))
    )
    private String userName;

    @Dimension(title = "Login Time", sql = "login_time")
    @EruptField(
            views = @View(title = "Login Time", sortable = true),
            edit = @Edit(title = "Login Time", search = @Search, dateType = @DateType(type = DateType.Type.DATE_TIME))
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
            views = @View(title = "IP Source", desc = "Country | Province | City | ISP | Country Code", width = "300px", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP Source", search = @Search)
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
    private Long count;

}
