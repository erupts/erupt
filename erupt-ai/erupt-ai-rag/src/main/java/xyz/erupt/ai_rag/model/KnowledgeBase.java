package xyz.erupt.ai_rag.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.model.EmbeddingLLM;
import xyz.erupt.ai_rag.handler.KnowledgeBaseDataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Erupt(
        name = "Knowledge Base", dataProxy = KnowledgeBaseDataProxy.class,
        rowOperation = @RowOperation(title = "Retrieval Test", icon = "fa fa-search",
                tpl = @Tpl(path = "/tpl/rag-retrieval.ftl", height = "85vh"),
                mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL)
)
@Getter
@Setter
@Table(name = "e_ai_kb")
@Entity
@EruptI18n
public class KnowledgeBase extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name", width = "150px"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "embedding_llm_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Embedding Model", column = "name"),
            edit = @Edit(title = "Embedding Model", notNull = true, type = EditType.REFERENCE_TABLE,
                    desc = "Changing it after documents are embedded invalidates existing vectors — re-embed all documents afterwards")
    )
    private EmbeddingLLM embeddingLlm;

    @EruptField(
            views = @View(title = "Chunk Size"),
            edit = @Edit(title = "Chunk Size", notNull = true, desc = "Max characters per chunk")
    )
    private Integer chunkSize = 500;

    @EruptField(
            views = @View(title = "Chunk Overlap"),
            edit = @Edit(title = "Chunk Overlap", notNull = true, desc = "Characters shared by adjacent chunks")
    )
    private Integer chunkOverlap = 50;

    @EruptField(
            views = @View(title = "Top K"),
            edit = @Edit(title = "Top K", notNull = true, desc = "Default number of chunks returned per retrieval")
    )
    private Integer topK = 5;

    @EruptField(
            views = @View(title = "Min Score"),
            edit = @Edit(title = "Min Score", notNull = true, desc = "Similarity threshold between 0 and 1; lower scored chunks are dropped")
    )
    private Double minScore = 0.5;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", search = @Search, type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA,
                    desc = "Also read by AI agents to decide which knowledge base fits a question — describe the content clearly")
    )
    private String remark;

}
