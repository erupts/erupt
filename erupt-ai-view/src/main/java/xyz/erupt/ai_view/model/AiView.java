package xyz.erupt.ai_view.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_view.handler.AiViewGenerateHandler;
import xyz.erupt.ai_view.handler.EruptNameTagsHandler;
import xyz.erupt.ai_view.proxy.AiViewDataProxy;
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
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * An AI generated view: the user describes a page in natural language, the LLM
 * writes a self-contained HTML page that reads data through the Erupt REST API.
 * The page is stored in the database and served by {@code AiViewController}.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Erupt(
        name = "AI View",
        dataProxy = AiViewDataProxy.class,
        rowOperation = {
                @RowOperation(title = "Generate", icon = "fa fa-magic",
                        mode = RowOperation.Mode.SINGLE,
                        operationHandler = AiViewGenerateHandler.class,
                        callHint = "Generate the page with AI now? The current HTML will be overwritten."),
                @RowOperation(title = "Preview", icon = "fa fa-eye",
                        mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL,
                        tpl = @Tpl(path = "/tpl/ai-view-preview.ftl", width = "94%", height = "94%"))
        }
)
@Table(name = "e_ai_view")
@Getter
@Setter
@Entity
@EruptI18n
public class AiView extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Target Models"),
            edit = @Edit(title = "Target Models", notNull = true, type = EditType.TAGS,
                    desc = "Erupt models the page reads from; their field structures are sent to the AI",
                    tagsType = @TagsType(fetchHandler = EruptNameTagsHandler.class, joinSeparator = ",", allowExtension = false))
    )
    private String targetErupts;

    @ManyToOne
    @JoinColumn(name = "llm_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Model", column = "name"),
            edit = @Edit(title = "Model", type = EditType.REFERENCE_TABLE,
                    desc = "Leave blank to use the default chat model")
    )
    private LLM llm;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "Requirement", notNull = true, type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "markdown"),
                    desc = "Describe the page in natural language; edit and regenerate to refine")
    )
    private String requirement;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "HTML", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "html"),
                    desc = "Generated page source; manual tweaks are kept until the next generation")
    )
    private String html;

    @EruptField(
            views = @View(title = "Enable"),
            edit = @Edit(title = "Enable", notNull = true,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    // Render path filled by AiViewDataProxy; mount it as a 'Link' type menu to expose the page
    @Transient
    @EruptField(views = @View(title = "Path"))
    private String path;

}
