package xyz.erupt.comment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.comment.handler.CommentEruptChoice;

/**
 * One comment on one record of any erupt model. The record is addressed by model name plus
 * primary key rendered as a string, so the table serves every model regardless of key type.
 * Threads are one level deep: a reply points at the top-level comment it answers.
 */
@EruptI18n
@Erupt(
        name = "Record Comment",
        orderBy = "createTime desc",
        power = @Power(add = false, edit = false, export = true)
)
@Entity
@Table(name = "e_record_comment", indexes = @Index(columnList = "erupt, recordId"))
@Getter
@Setter
public class EruptRecordComment extends xyz.erupt.upms.helper.HyperModelCreatorOnlyVo {

    // stored as the erupt name ("nodeName.eruptName" for a cloud node model); shown and filtered by
    // the data model's own translated name, which is what the choice list labels it with
    @Column(length = 100)
    @EruptField(
            views = @View(title = "Model", width = "160px"),
            edit = @Edit(title = "Model", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = CommentEruptChoice.class), search = @Search)
    )
    private String erupt;

    @Column(length = 100)
    @EruptField(
            views = @View(title = "Record ID", width = "120px"),
            edit = @Edit(title = "Record ID", notNull = true, search = @Search)
    )
    private String recordId;

    @Column(length = 4000)
    @EruptField(
            views = @View(title = "Content"),
            edit = @Edit(title = "Content", type = EditType.TEXTAREA, notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String content;

    @EruptField(
            views = @View(title = "Reply To", width = "100px", show = false),
            edit = @Edit(title = "Reply To", show = false)
    )
    private Long parentId;

    // users named with @ in the content, as JSON [{id, name}]; the names let the UI highlight them
    @Column(length = 2000)
    @EruptField(
            views = @View(title = "Mentions", show = false),
            edit = @Edit(title = "Mentions", show = false)
    )
    private String mentions;

    // thread heads only: a resolved thread is shown collapsed
    @EruptField(
            views = @View(title = "Resolved", width = "80px"),
            edit = @Edit(title = "Resolved", type = EditType.BOOLEAN, show = false)
    )
    private Boolean resolved = false;

    // thread heads only: pinned threads lead the stream
    @EruptField(
            views = @View(title = "Pinned", width = "80px"),
            edit = @Edit(title = "Pinned", type = EditType.BOOLEAN, show = false)
    )
    private Boolean pinned = false;

}
