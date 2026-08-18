package xyz.erupt.ai_rag.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_rag.handler.KnowledgeChunkDataProxy;
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
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Chunk text is the single source of truth here; the vector store only holds
 * id + vector. Editing the text re-embeds it, so both never drift apart.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Erupt(
        name = "Knowledge Chunk", dataProxy = KnowledgeChunkDataProxy.class,
        power = @Power(add = false, importable = false),
        orderBy = "id"
)
@Getter
@Setter
@Table(name = "e_ai_kb_chunk")
@Entity
@EruptI18n
public class KnowledgeChunk extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "document_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Document", column = "name")
    )
    private KnowledgeDocument document;

    @EruptField(
            views = @View(title = "Seq", width = "80px", sortable = true),
            edit = @Edit(title = "Seq", readonly = @Readonly)
    )
    private Integer seq;

    @Lob
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Text"),
            edit = @Edit(title = "Text", notNull = true, type = EditType.TEXTAREA,
                    search = @Search(operator = QueryExpression.LIKE),
                    desc = "Saving re-embeds this chunk so text and vector stay consistent")
    )
    private String text;

    @EruptField(
            views = @View(title = "Vector ID", show = false)
    )
    private String vectorId;

}
