package xyz.erupt.revision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
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
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.revision.handler.RevisionEruptChoice;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

/**
 * One revision of one record of any erupt model. The record is addressed by model name plus
 * primary key rendered as a string, so the table serves every model regardless of key type.
 * {@code changes} holds the field-level diff as a JSON array of {@code {field, title, before, after}};
 * an ADD lists every non-empty field with {@code before} absent, a DELETE every field with
 * {@code after} absent. Values are the record's masked JSON, so a password never lands here.
 * Revisions are written by the pipeline only, never by hand: the admin table is read-only.
 */
@EruptI18n
@Erupt(
        name = "Record Revision",
        orderBy = "createTime desc",
        power = @Power(add = false, edit = false, delete = false, export = true, revision = false, comment = false)
)
@Entity
@Table(name = "e_record_revision", indexes = @Index(columnList = "erupt, recordId"))
@Getter
@Setter
public class EruptRecordRevision extends HyperModelCreatorOnlyVo {

    // the erupt name ("nodeName.eruptName" for a cloud node model); shown and filtered by the
    // data model's own translated name, which is what the choice list labels it with
    @Column(length = 100)
    @EruptField(
            views = @View(title = "Model", width = "160px"),
            edit = @Edit(title = "Model", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = RevisionEruptChoice.class), search = @Search)
    )
    private String erupt;

    @Column(length = 100)
    @EruptField(
            views = @View(title = "Record ID", width = "120px"),
            edit = @Edit(title = "Record ID", notNull = true, search = @Search)
    )
    private String recordId;

    // 1-based, increasing per record
    @EruptField(
            views = @View(title = "Version", width = "80px", sortable = true),
            edit = @Edit(title = "Version")
    )
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Operation", width = "100px"),
            edit = @Edit(title = "Operation", search = @Search)
    )
    private RevisionOperation operation;

    // display name of whoever made the change, snapshotted so a removed user still reads well
    @Column(length = 100)
    @EruptField(
            views = @View(title = "Operator", width = "120px"),
            edit = @Edit(title = "Operator", search = @Search)
    )
    private String operator;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Changes", type = ViewType.CODE),
            edit = @Edit(title = "Changes", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String changes;

}
