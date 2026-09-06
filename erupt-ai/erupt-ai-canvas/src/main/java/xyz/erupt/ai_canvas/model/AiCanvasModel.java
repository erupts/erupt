package xyz.erupt.ai_canvas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_canvas.handler.CanvasDataTypeFetchHandler;
import xyz.erupt.ai_canvas.handler.CanvasTargetModelFetchHandler;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.jpa.model.BaseModel;

/**
 * One data model bound to an {@link AiCanvas}, edited as a MULTI_FORM block on
 * the canvas form. Each binding carries its own data source type, so one page
 * may read from several providers; the service groups bindings by type when it
 * composes the generation prompt. The allow* switches declare which write
 * operations the generated page may offer on the model (all off = read-only);
 * the visitor's own permissions are still enforced server-side on every call.
 *
 * @author YuePeng
 * date 2026/9/6
 */
@Erupt(name = "AI Canvas Model")
@Table(name = "e_ai_canvas_model")
@Getter
@Setter
@Entity
@EruptI18n
public class AiCanvasModel extends BaseModel {

    // Owning canvas, mapped by the unidirectional @JoinColumn on AiCanvas.models;
    // exposed read-only so the list view can resolve bindings per canvas
    @Column(name = "canvas_id", insertable = false, updatable = false)
    private Long canvasId;

    @EruptField(
            views = @View(title = "Data Type"),
            edit = @Edit(title = "Data Type", notNull = true,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = CanvasDataTypeFetchHandler.class))
    )
    private String dataType;

    @EruptField(
            views = @View(title = "Data Model"),
            edit = @Edit(title = "Data Model", notNull = true,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = CanvasTargetModelFetchHandler.class,
                            dependField = "dataType"))
    )
    private String model;

    // Optional hint on the model's role in the page (e.g. "order lines of the
    // selected order"), injected into the prompt next to the model structure
    @EruptField(
            views = @View(title = "Purpose"),
            edit = @Edit(title = "Purpose", inputType = @InputType(fullSpan = true))
    )
    private String purpose;

    @EruptField(
            views = @View(title = "Allow Add"),
            edit = @Edit(title = "Allow Add", notNull = true)
    )
    private Boolean allowAdd = false;

    @EruptField(
            views = @View(title = "Allow Edit"),
            edit = @Edit(title = "Allow Edit", notNull = true)
    )
    private Boolean allowEdit = false;

    @EruptField(
            views = @View(title = "Allow Delete"),
            edit = @Edit(title = "Allow Delete", notNull = true)
    )
    private Boolean allowDelete = false;

}
