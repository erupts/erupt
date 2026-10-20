package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * What a module last seeded into the menu table, kept in the database rather than in a marker
 * file so containers, clusters and copied databases all agree on it.
 * <p>
 * {@code fingerprint} hashes the module's menu definitions: while it matches, startup skips the
 * module with no further query. {@code snapshot} holds, per menu code, the values the module wrote
 * last time; a menu field still equal to it was not touched by anyone, so a changed definition may
 * replace it, while a field an admin edited keeps the edit.
 * <p>
 * Rows that are not menu modules use the same table for one-off work ({@code seededAt}, such as the
 * default admin account) or a small value kept per database ({@code payload}, such as the telemetry
 * instance id); see EruptModuleStore.
 *
 * @author YuePeng
 */
@Entity
@Table(name = "e_upms_module")
@Getter
@Setter
public class EruptModuleSeed extends BaseModel {

    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false, unique = true)
    private String module;

    @Column(length = 64)
    private String fingerprint;

    // the erupt version that last seeded the module, for whoever reads the table
    @Column(length = 32)
    private String eruptVersion;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String snapshot;

    // null until the module's first seeding (or one-off task) has run
    private LocalDateTime seededAt;

    // a small value the module keeps per database
    @Column(length = 255)
    private String payload;

}
