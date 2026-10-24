package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.TenantId;
import xyz.erupt.ai_dev.constant.ReviewDecision;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;

/**
 * One review of a requirement: who approved or rejected it and why. A rejection keeps the reason the
 * creator has to answer; an approval may carry a note for the agent.
 *
 * @author YuePeng
 * date 2026/10/24
 */
@Table(name = "e_ai_dev_requirement_review", indexes = @Index(name = "idx_ai_dev_review_requirement", columnList = "requirement_id"))
@Getter
@Setter
@Entity
public class DevRequirementReview extends BaseModel {

    @TenantId
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "requirement_id", nullable = false, foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevRequirement requirement;

    @Enumerated(EnumType.STRING)
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false)
    private ReviewDecision decision;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private EruptUser reviewer;

    private LocalDateTime reviewedAt;

}
