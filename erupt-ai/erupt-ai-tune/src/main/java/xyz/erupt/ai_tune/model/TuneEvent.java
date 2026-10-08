package xyz.erupt.ai_tune.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_tune.constants.EventLevel;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * A line of a job's training log. Provider events are mirrored here together with the
 * metrics they carry, so the loss curve survives the provider's own retention window
 * and the local lifecycle (upload, submit, cancel) is logged in the same stream.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Erupt(
        name = "Training Event", orderBy = "createdAt desc, id desc",
        power = @Power(add = false, edit = false, delete = false, viewDetails = true, export = true)
)
@Getter
@Setter
@Table(name = "e_ai_tune_event", indexes = @Index(name = "idx_tune_event_job", columnList = "job_id"))
@Entity
@EruptI18n
public class TuneEvent extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "job_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Job", column = "name", show = false)
    )
    private TuneJob job;

    @EruptField(
            views = @View(title = "Time", type = ViewType.DATE_TIME, width = "160px", sortable = true)
    )
    private LocalDateTime createdAt;

    @EruptField(
            views = @View(title = "Level", width = "80px"),
            edit = @Edit(title = "Level", type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = EventLevel.INFO, label = "Info"),
                            @VL(value = EventLevel.WARN, label = "Warn"),
                            @VL(value = EventLevel.ERROR, label = "Error")
                    }))
    )
    private String level = EventLevel.INFO;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Message")
    )
    private String message;

    @EruptField(
            views = @View(title = "Step", width = "80px")
    )
    private Integer step;

    @EruptField(
            views = @View(title = "Train Loss", width = "100px")
    )
    private Double trainLoss;

    @EruptField(
            views = @View(title = "Valid Loss", width = "100px")
    )
    private Double validLoss;

    @EruptField(
            views = @View(title = "Train Accuracy", width = "120px")
    )
    private Double trainAccuracy;

    // Provider's own event id, so repeated polls never insert the same event twice
    @Column(length = 128)
    private String remoteId;

    public static TuneEvent of(TuneJob job, String level, String message) {
        TuneEvent event = new TuneEvent();
        event.setJob(job);
        event.setCreatedAt(LocalDateTime.now());
        event.setLevel(level);
        event.setMessage(message);
        return event;
    }

}
