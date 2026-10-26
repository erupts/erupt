package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_dev.constant.AgentRole;
import xyz.erupt.ai_dev.constant.SessionStatus;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * One run of a coding agent on a requirement. {@link #sessionId} is the agent CLI's own session, so a
 * retry continues the conversation instead of starting over.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Table(name = "e_ai_dev_agent_session")
@Getter
@Setter
@Entity
public class DevAgentSession extends BaseModel {

    // denormalised so access checks reach the project without a join
    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevProject project;

    @ManyToOne
    @JoinColumn(name = "requirement_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevRequirement requirement;

    @Enumerated(EnumType.STRING)
    private AgentRole role;

    private String agentName;

    @Column(length = AnnotationConst.CODE_LENGTH)
    private String executor;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private String sessionId;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String summary;

    private Double costUsd;

    private Long tokens;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

}
