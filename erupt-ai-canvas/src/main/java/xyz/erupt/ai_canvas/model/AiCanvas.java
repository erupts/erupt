package xyz.erupt.ai_canvas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_canvas.proxy.AiCanvasDataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.OpenWay;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * An AI generated view, built conversationally in the designer: pick a data
 * model, describe the page, iterate over versions. The active version's HTML
 * is stored here and served by {@code AiCanvasController}.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Erupt(
        name = "AI Canvas",
        dataProxy = AiCanvasDataProxy.class,
        rowOperation = @RowOperation(title = "Designer", icon = "fa fa-paint-brush",
                mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL,
                tpl = @Tpl(path = "/ai/canvas/{id}", openWay = OpenWay.ROUTER))
)
@Table(name = "e_ai_canvas")
@Getter
@Setter
@Entity
@EruptI18n
public class AiCanvas extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    // Data source type + model, chosen in the designer conversation panel
    @EruptField(
            views = @View(title = "Data Type", width = "90px")
    )
    private String dataType;

    @EruptField(
            views = @View(title = "Data Model", width = "150px")
    )
    private String targetModel;

    // Page style code from prompts/style.json, chosen in the designer
    @EruptField(
            views = @View(title = "Style", width = "110px")
    )
    private String style;

    @ManyToOne
    @JoinColumn(name = "llm_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Model", column = "name"),
            edit = @Edit(title = "Model", type = EditType.REFERENCE_TABLE,
                    desc = "Leave blank to use the default chat model")
    )
    private LLM llm;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
//    @EruptField(
//            edit = @Edit(title = "HTML", type = EditType.CODE_EDITOR,
//                    codeEditType = @CodeEditorType(language = "html"),
//                    desc = "Active version's page source; manual tweaks are kept until the next generation")
//    )
    private String html;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Description", type = ViewType.HTML),
            edit = @Edit(title = "Description", type = EditType.TEXTAREA)
    )
    private String remark;

    // Currently active version row id
    private Long activeVersion;

    @EruptField(
            views = @View(title = "Enable"),
            edit = @Edit(title = "Enable", notNull = true,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    // Render path filled by AiCanvasDataProxy; mount it as a 'Link' type menu to expose the page
    @Transient
    @EruptField(views = @View(title = "Path"))
    private String path;

}
