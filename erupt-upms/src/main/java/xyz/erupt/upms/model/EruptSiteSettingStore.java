package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.helper.UpmsTenant;

/**
 * Where the site settings are kept: one row per owner, the whole {@link EruptSiteSetting} form as
 * one JSON document. Settings are added often, and a document takes a new one without DDL — a
 * deployment that runs with schema generation off never has to alter this table again.
 * <p>
 * {@code tenantId} is the owner, handled as in the user directory ({@link EruptUser}): Hibernate
 * restricts every query to the session's tenant and stamps it on insert, so a tenant session reads
 * and writes its own row without a condition in erupt's code, and the platform is
 * {@link UpmsTenant#PLATFORM}. One row per tenant.
 *
 * @author YuePeng
 * date 2026/11/23
 */
@Entity
@Table(name = "e_upms_site_setting", uniqueConstraints = @UniqueConstraint(name = "uk_upms_site_setting_tenant", columnNames = {"tenantId"}))
@Getter
@Setter
public class EruptSiteSettingStore extends MetaModelUpdateVo {

    // Owner tenant, UpmsTenant.PLATFORM for the platform, restricted and stamped by Hibernate
    // (see UpmsTenantIdentifierResolver)
    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    // The form as the frontend posts it, minus what is not a setting (id, audit fields); a
    // referenced menu is kept by id alone
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String settings;

}
