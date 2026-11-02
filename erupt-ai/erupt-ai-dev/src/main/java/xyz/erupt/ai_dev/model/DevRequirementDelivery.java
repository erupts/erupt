package xyz.erupt.ai_dev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_dev.constant.DeliveryStatus;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * Delivery of one requirement to one repository: the branch pushed and how it went. Written by the
 * delivery step only.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Table(name = "e_ai_dev_requirement_delivery")
@Getter
@Setter
@Entity
public class DevRequirementDelivery extends BaseModel {

    // denormalised so access checks reach the project without a join
    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevProject project;

    @ManyToOne
    @JoinColumn(name = "requirement_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private DevRequirement requirement;

    private String repoAlias;

    private String branch;

    private String commitSha;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    private String message;

    private LocalDateTime deliveredAt;

}
