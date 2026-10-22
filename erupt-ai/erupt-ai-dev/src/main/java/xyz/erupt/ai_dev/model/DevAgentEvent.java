package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;

import java.time.LocalDateTime;

/**
 * One line of an agent session's log: what it said, which tool it called with what, and what came back.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Table(name = "e_ai_dev_agent_event", indexes = @Index(name = "idx_ai_dev_event_session", columnList = "session_id, seq"))
@Getter
@Setter
@Entity
public class DevAgentEvent extends BaseModel {

    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevProject project;

    @ManyToOne
    @JoinColumn(name = "session_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevAgentSession session;

    private Integer seq;

    @Enumerated(EnumType.STRING)
    private EventKind kind;

    private String name;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String content;

    private LocalDateTime createdAt;

}
