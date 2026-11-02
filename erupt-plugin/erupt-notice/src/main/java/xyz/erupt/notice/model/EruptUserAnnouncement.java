package xyz.erupt.notice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;

/**
 * @author YuePeng
 * date 2025/11/28 23:41
 */
@Entity
@Table(name = "e_upms_user")
@Getter
@Setter
public class EruptUserAnnouncement extends BaseModel {

    // same table as EruptUser: the tenant restriction has to apply to this view as well
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    private Long annReadId;

}
