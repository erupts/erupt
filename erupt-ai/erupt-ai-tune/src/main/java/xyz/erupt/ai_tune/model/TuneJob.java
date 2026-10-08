package xyz.erupt.ai_tune.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_tune.constants.TuneMethod;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.handler.TuneJobDataProxy;
import xyz.erupt.ai_tune.handler.TuneJobOperationHandler;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import java.time.LocalDateTime;

/**
 * A fine-tuning job: which base model, which datasets, which hyperparameters, and the
 * mirror of what the provider reports while it trains. The provider is derived from the
 * base model's LLM record, so the API key configured once for chat is reused for training.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Erupt(
        name = "Fine-tuning Job", dataProxy = TuneJobDataProxy.class,
        orderBy = "id desc",
        layout = @Layout(tableLeftFixed = 1, formSteps = true),
        drills = @Drill(title = "Events", icon = "fa fa-stream",
                link = @Link(linkErupt = TuneEvent.class, joinColumn = "job.id")),
        rowOperation = {
                @RowOperation(title = "Monitor", icon = "fa fa-chart-line",
                        tpl = @Tpl(path = "/tpl/tune-monitor.ftl", height = "88vh", width = "1200px"),
                        mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL),
                @RowOperation(title = "Start Training", icon = "fa fa-play",
                        callHint = "tune.start_hint",
                        ifExpr = "['DRAFT','FAILED','CANCELLED'].indexOf(item.status) >= 0",
                        ifExprBehavior = RowOperation.IfExprBehavior.HIDE,
                        mode = RowOperation.Mode.SINGLE, operationParam = TuneJobOperationHandler.START,
                        operationHandler = TuneJobOperationHandler.class),
                @RowOperation(title = "Cancel", icon = "fa fa-stop",
                        callHint = "tune.cancel_hint",
                        ifExpr = "['VALIDATING','QUEUED','RUNNING'].indexOf(item.status) >= 0",
                        ifExprBehavior = RowOperation.IfExprBehavior.HIDE,
                        mode = RowOperation.Mode.SINGLE, operationParam = TuneJobOperationHandler.CANCEL,
                        operationHandler = TuneJobOperationHandler.class),
                @RowOperation(title = "Sync Now", icon = "fa fa-rotate", callHint = "",
                        ifExpr = "!!item.remoteJobId",
                        ifExprBehavior = RowOperation.IfExprBehavior.HIDE,
                        mode = RowOperation.Mode.MULTI, operationParam = TuneJobOperationHandler.SYNC,
                        operationHandler = TuneJobOperationHandler.class),
                @RowOperation(title = "Compare", icon = "fa fa-columns",
                        tip = "Send the same prompt to the base and the tuned model side by side",
                        tpl = @Tpl(path = "/tpl/tune-compare.ftl", height = "88vh", width = "1200px"),
                        ifExpr = "item.status === 'SUCCEEDED'",
                        ifExprBehavior = RowOperation.IfExprBehavior.HIDE,
                        mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL),
                @RowOperation(title = "Register as LLM", icon = "fa fa-plug",
                        tip = "Create an LLM record for the tuned model so it can be picked in AI Chat and agents",
                        callHint = "tune.register_hint",
                        ifExpr = "item.status === 'SUCCEEDED' && !item.registeredLlm_name",
                        ifExprBehavior = RowOperation.IfExprBehavior.HIDE,
                        mode = RowOperation.Mode.SINGLE, operationParam = TuneJobOperationHandler.REGISTER,
                        operationHandler = TuneJobOperationHandler.class)
        }
)
@Getter
@Setter
@Table(name = "e_ai_tune_job")
@Entity
@EruptI18n
public class TuneJob extends MetaModelUpdateVo {

    @Transient
    @EruptField(
            edit = @Edit(title = "Basic Info", desc = "Base model and training data", type = EditType.DIVIDE)
    )
    private String basicStep;

    @EruptField(
            views = @View(title = "Job Name", width = "180px"),
            edit = @Edit(title = "Job Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "base_llm_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Provider", column = "llm", width = "110px"),
            edit = @Edit(title = "Base LLM", notNull = true, type = EditType.REFERENCE_TABLE,
                    onchange = TuneJobDataProxy.class,
                    desc = "Provider, endpoint and API key are taken from this LLM record")
    )
    private LLM baseLlm;

    @EruptField(
            views = @View(title = "Base Model", width = "160px"),
            edit = @Edit(title = "Base Model", notNull = true,
                    desc = "Model id to fine-tune, e.g. gpt-4o-mini-2024-07-18, glm-4-flash, qwen-turbo; prefilled from the LLM record")
    )
    private String baseModel;

    @EruptField(
            views = @View(title = "Method", width = "80px"),
            edit = @Edit(title = "Method", type = EditType.CHOICE, notNull = true, search = @Search,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = TuneMethod.SFT, label = "Supervised (SFT)"),
                            @VL(value = TuneMethod.DPO, label = "Preference (DPO)")
                    }))
    )
    private String method = TuneMethod.SFT;

    @ManyToOne
    @JoinColumn(name = "training_dataset_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Training Dataset", column = "name", width = "160px"),
            edit = @Edit(title = "Training Dataset", notNull = true, type = EditType.REFERENCE_TABLE,
                    filter = @Filter("status = 'READY'"))
    )
    private TuneDataset trainingDataset;

    @ManyToOne
    @JoinColumn(name = "validation_dataset_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Validation Dataset", column = "name", show = false),
            edit = @Edit(title = "Validation Dataset", type = EditType.REFERENCE_TABLE,
                    filter = @Filter("status = 'READY'"),
                    desc = "Optional held-out set; its loss is reported next to the training loss")
    )
    private TuneDataset validationDataset;

    @EruptField(
            views = @View(title = "Suffix", show = false),
            edit = @Edit(title = "Suffix", desc = "Short tag embedded in the tuned model's name, e.g. support-bot")
    )
    private String suffix;

    @Transient
    @EruptField(
            edit = @Edit(title = "Hyperparameters", desc = "Leave a field blank to let the provider choose", type = EditType.DIVIDE)
    )
    private String hyperStep;

    @EruptField(
            views = @View(title = "Epochs", show = false),
            edit = @Edit(title = "Epochs", type = EditType.NUMBER, numberType = @NumberType(min = 1, max = 50),
                    desc = "Passes over the training set; 2-4 is typical")
    )
    private Integer epochs;

    @EruptField(
            views = @View(title = "Learning Rate Multiplier", show = false),
            edit = @Edit(title = "Learning Rate Multiplier", type = EditType.NUMBER,
                    numberType = @NumberType(min = 0, max = 10, precision = 2, step = 0.1),
                    desc = "Scales the provider's default learning rate; lower values reduce overfitting")
    )
    private Double learningRateMultiplier;

    @EruptField(
            views = @View(title = "Batch Size", show = false),
            edit = @Edit(title = "Batch Size", type = EditType.NUMBER, numberType = @NumberType(min = 1, max = 256))
    )
    private Integer batchSize;

    @EruptField(
            views = @View(title = "DPO Beta", show = false),
            edit = @Edit(title = "DPO Beta", type = EditType.NUMBER, numberType = @NumberType(min = 0, max = 2, precision = 2, step = 0.05),
                    dynamic = @Dynamic(dependField = "method", condition = "value === 'DPO'"),
                    desc = "How strongly the tuned model stays close to the base model; 0.1 is a common default")
    )
    private Double dpoBeta;

    @EruptField(
            views = @View(title = "Seed", show = false),
            edit = @Edit(title = "Seed", type = EditType.NUMBER, desc = "Fix it to make two runs comparable")
    )
    private Integer seed;

    @Transient
    @EruptField(
            edit = @Edit(title = "Run State", desc = "Filled in by the provider while the job runs", type = EditType.DIVIDE)
    )
    private String stateStep;

    @EruptField(
            views = @View(title = "Status", sortable = true, width = "110px"),
            edit = @Edit(title = "Status", type = EditType.CHOICE, search = @Search, readonly = @Readonly,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = TuneStatus.DRAFT, label = "Draft"),
                            @VL(value = TuneStatus.UPLOADING, label = "Uploading"),
                            @VL(value = TuneStatus.VALIDATING, label = "Validating Files"),
                            @VL(value = TuneStatus.QUEUED, label = "Queued"),
                            @VL(value = TuneStatus.RUNNING, label = "Running"),
                            @VL(value = TuneStatus.SUCCEEDED, label = "Succeeded"),
                            @VL(value = TuneStatus.FAILED, label = "Failed"),
                            @VL(value = TuneStatus.CANCELLED, label = "Cancelled")
                    }))
    )
    private String status = TuneStatus.DRAFT;

    @EruptField(
            views = @View(title = "Fine-tuned Model", width = "260px"),
            edit = @Edit(title = "Fine-tuned Model", readonly = @Readonly)
    )
    private String fineTunedModel;

    @EruptField(
            views = @View(title = "Remote Job ID", show = false),
            edit = @Edit(title = "Remote Job ID", readonly = @Readonly)
    )
    private String remoteJobId;

    @EruptField(
            views = @View(title = "Training File ID", show = false),
            edit = @Edit(title = "Training File ID", readonly = @Readonly)
    )
    private String trainingFileId;

    @EruptField(
            views = @View(title = "Validation File ID", show = false),
            edit = @Edit(title = "Validation File ID", readonly = @Readonly)
    )
    private String validationFileId;

    @EruptField(
            views = @View(title = "Trained Tokens", show = false),
            edit = @Edit(title = "Trained Tokens", readonly = @Readonly)
    )
    private Long trainedTokens;

    @EruptField(
            views = @View(title = "Started At", type = ViewType.DATE_TIME, width = "160px"),
            edit = @Edit(title = "Started At", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime startedAt;

    @EruptField(
            views = @View(title = "Finished At", type = ViewType.DATE_TIME, show = false),
            edit = @Edit(title = "Finished At", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime finishedAt;

    @EruptField(
            views = @View(title = "Estimated Finish", type = ViewType.DATE_TIME, show = false),
            edit = @Edit(title = "Estimated Finish", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime estimatedFinish;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Error Info", show = false),
            edit = @Edit(title = "Error Info", readonly = @Readonly, type = EditType.TEXTAREA)
    )
    private String errorInfo;

    // JSON array of provider checkpoints: [{"id","step","model","metrics":{...}}]
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "Checkpoints", show = false)
    )
    private String checkpoints;

    @ManyToOne
    @JoinColumn(name = "registered_llm_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Registered LLM", column = "name", width = "150px"),
            edit = @Edit(title = "Registered LLM", type = EditType.REFERENCE_TABLE, readonly = @Readonly)
    )
    private LLM registeredLlm;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark", show = false),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
