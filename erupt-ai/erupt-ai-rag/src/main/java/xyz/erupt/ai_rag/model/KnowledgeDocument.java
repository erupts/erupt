package xyz.erupt.ai_rag.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_rag.constants.DocStatus;
import xyz.erupt.ai_rag.handler.KnowledgeDocumentDataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Erupt(
        name = "Knowledge Document", dataProxy = KnowledgeDocumentDataProxy.class,
        orderBy = "id desc",
        linkTree = @LinkTree(field = "kb", dependNode = true),
        drills = @Drill(title = "Knowledge Chunk", icon = "fa fa-puzzle-piece",
                link = @Link(linkErupt = KnowledgeChunk.class, joinColumn = "document.id")),
        rowOperation = @RowOperation(title = "Re-embed", icon = "fa fa-refresh",
                mode = RowOperation.Mode.MULTI, operationHandler = KnowledgeDocumentDataProxy.class)
)
@Getter
@Setter
@Table(name = "e_ai_kb_document")
@Entity
@EruptI18n
public class KnowledgeDocument extends MetaModelUpdateVo {

    @ManyToOne
    @JoinColumn(name = "kb_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Knowledge Base", column = "name")
    )
    private KnowledgeBase kb;

    @EruptField(
            views = @View(title = "Name", width = "200px"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Attachment"),
            edit = @Edit(title = "Attachment", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(fileTypes = {"txt", "md", "markdown"}),
                    desc = "Plain text or markdown file; takes precedence over inline content")
    )
    private String attachment;

    @Lob
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "Content", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"),
                    desc = "Used when no attachment is uploaded")
    )
    private String content;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", type = EditType.CHOICE, search = @Search,
                    readonly = @xyz.erupt.annotation.sub_field.Readonly,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = DocStatus.PENDING, label = "Pending"),
                            @VL(value = DocStatus.EMBEDDING, label = "Embedding"),
                            @VL(value = DocStatus.READY, label = "Ready"),
                            @VL(value = DocStatus.FAILED, label = "Failed")
                    }))
    )
    private String status = DocStatus.PENDING;

    @EruptField(
            views = @View(title = "Chunks")
    )
    private Integer chunkCount;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Error Info")
    )
    private String errorInfo;

}
