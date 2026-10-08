package xyz.erupt.ai_tune.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_tune.constants.DatasetFormat;
import xyz.erupt.ai_tune.constants.DatasetStatus;
import xyz.erupt.ai_tune.handler.ChatHarvestHandler;
import xyz.erupt.ai_tune.handler.TuneDatasetDataProxy;
import xyz.erupt.ai_tune.model.input.ChatHarvestForm;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * A training dataset. Samples live one per row in {@link TuneSample}; the dataset is the
 * container that knows the wire format, the validation outcome and the aggregate numbers
 * every fine-tuning console shows before a job is allowed to start.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Erupt(
        name = "Training Dataset", dataProxy = TuneDatasetDataProxy.class,
        orderBy = "id desc",
        drills = @Drill(title = "Samples", icon = "fa fa-list",
                link = @Link(linkErupt = TuneSample.class, joinColumn = "dataset.id")),
        rowOperation = {
                @RowOperation(title = "Preview", icon = "fa fa-eye",
                        tpl = @Tpl(path = "/tpl/tune-dataset.ftl", height = "85vh", width = "1100px"),
                        mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL),
                @RowOperation(title = "Harvest Chat History", icon = "fa fa-comments",
                        tip = "Turn past conversations from AI Chat into training samples",
                        mode = RowOperation.Mode.SINGLE, eruptClass = ChatHarvestForm.class,
                        operationHandler = ChatHarvestHandler.class),
                @RowOperation(title = "Re-validate", icon = "fa fa-check-double",
                        mode = RowOperation.Mode.MULTI, operationHandler = TuneDatasetDataProxy.class)
        }
)
@Getter
@Setter
@Table(name = "e_ai_tune_dataset")
@Entity
@EruptI18n
public class TuneDataset extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name", width = "180px"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Format"),
            edit = @Edit(title = "Format", type = EditType.CHOICE, notNull = true, search = @Search,
                    desc = "Chat: supervised samples ending with an assistant turn. Preference: a prompt with a preferred and a non-preferred answer (DPO)",
                    choiceType = @ChoiceType(vl = {
                            @VL(value = DatasetFormat.CHAT, label = "Chat (SFT)"),
                            @VL(value = DatasetFormat.PREFERENCE, label = "Preference (DPO)")
                    }))
    )
    private String format = DatasetFormat.CHAT;

    @EruptField(
            views = @View(title = "JSONL File", type = ViewType.DOWNLOAD),
            edit = @Edit(title = "JSONL File", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(fileTypes = {"jsonl", "json", "txt"}),
                    desc = "One JSON object per line. Uploading a new file replaces every sample that was imported from a file; samples harvested from chat history or added by hand are kept")
    )
    private String attachment;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", type = EditType.CHOICE, search = @Search, readonly = @Readonly,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = DatasetStatus.PENDING, label = "Pending"),
                            @VL(value = DatasetStatus.VALIDATING, label = "Validating"),
                            @VL(value = DatasetStatus.READY, label = "Ready"),
                            @VL(value = DatasetStatus.FAILED, label = "Failed")
                    }))
    )
    private String status = DatasetStatus.PENDING;

    @EruptField(
            views = @View(title = "Samples", sortable = true),
            edit = @Edit(title = "Samples", readonly = @Readonly)
    )
    private Integer sampleCount;

    @EruptField(
            views = @View(title = "Valid"),
            edit = @Edit(title = "Valid", readonly = @Readonly)
    )
    private Integer validCount;

    @EruptField(
            views = @View(title = "Est. Tokens", sortable = true),
            edit = @Edit(title = "Est. Tokens", readonly = @Readonly,
                    desc = "Rough token count across valid samples; drives epoch and cost estimates")
    )
    private Long tokenEstimate;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Error Info"),
            edit = @Edit(title = "Error Info", readonly = @Readonly, type = EditType.TEXTAREA)
    )
    private String errorInfo;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
