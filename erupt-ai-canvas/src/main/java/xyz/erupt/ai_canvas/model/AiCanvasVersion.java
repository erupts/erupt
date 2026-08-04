package xyz.erupt.ai_canvas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelCreateOnlyVo;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

/**
 * One generation round of a view: the user message and the page it produced.
 * Append-only; the designer switches the active version by copying its html
 * back onto the AiCanvas.
 *
 * @author YuePeng
 * date 2026/8/4
 */
@Erupt(
        name = "Canvas Version",
        orderBy = "id desc",
        power = @Power(add = false, edit = false)
)
@Table(name = "e_ai_canvas_version")
@Getter
@Setter
@Entity
@EruptI18n
@NoArgsConstructor
public class AiCanvasVersion extends MetaModelCreateOnlyVo {

    @EruptField(
            views = @View(title = "Canvas"),
            edit = @Edit(title = "Canvas", search = @Search, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id, name from e_ai_canvas"))
    )
    private Long canvasId;

    @EruptField(
            views = @View(title = "Version", width = "80px")
    )
    private Integer version;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Requirement")
    )
    private String message;

    // Data source and style snapshot of this generation round
    @EruptField(
            views = @View(title = "Data Type", width = "90px")
    )
    private String dataType;

    @EruptField(
            views = @View(title = "Data Model", width = "150px")
    )
    private String targetModel;

    @EruptField(
            views = @View(title = "Style", width = "110px")
    )
    private String style;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String html;

    public AiCanvasVersion(AiCanvas view, Integer version, String message, String html) {
        this.canvasId = view.getId();
        this.version = version;
        this.message = message;
        this.html = html;
        this.dataType = view.getDataType();
        this.targetModel = view.getTargetModel();
        this.style = view.getStyle();
    }

}
