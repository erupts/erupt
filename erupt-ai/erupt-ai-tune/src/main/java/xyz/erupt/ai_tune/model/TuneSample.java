package xyz.erupt.ai_tune.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_tune.handler.TuneSampleDataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * One training sample, kept as the raw JSON object of its JSONL line. The line is the single
 * source of truth: validation, token estimates, the preview page and the exported file are all
 * derived from it, so hand edits in the code editor need no second representation.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Erupt(
        name = "Training Sample", dataProxy = TuneSampleDataProxy.class,
        orderBy = "seq",
        power = @Power(importable = false, export = true)
)
@Getter
@Setter
@Table(name = "e_ai_tune_sample", indexes = @Index(name = "idx_tune_sample_dataset", columnList = "dataset_id"))
@Entity
@EruptI18n
public class TuneSample extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "dataset_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Dataset", column = "name", show = false)
    )
    private TuneDataset dataset;

    @EruptField(
            views = @View(title = "Seq", width = "70px", sortable = true),
            edit = @Edit(title = "Seq", readonly = @Readonly)
    )
    private Integer seq;

    @EruptField(
            views = @View(title = "Source", width = "90px"),
            edit = @Edit(title = "Source", readonly = @Readonly, search = @Search)
    )
    private String source;

    @Lob
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Content", width = "520px"),
            edit = @Edit(title = "Content", type = EditType.CODE_EDITOR, notNull = true,
                    codeEditType = @CodeEditorType(language = "json"),
                    search = @Search(operator = QueryExpression.LIKE))
    )
    private String content;

    @EruptField(
            views = @View(title = "Turns", width = "70px")
    )
    private Integer turns;

    @EruptField(
            views = @View(title = "Est. Tokens", width = "100px", sortable = true)
    )
    private Integer tokens;

    @EruptField(
            views = @View(title = "Valid", width = "80px", sortable = true),
            edit = @Edit(title = "Valid", type = EditType.BOOLEAN, readonly = @Readonly, search = @Search,
                    boolType = @BoolType(trueText = "Valid", falseText = "Invalid"))
    )
    private Boolean valid;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Error Info")
    )
    private String errorInfo;

}
