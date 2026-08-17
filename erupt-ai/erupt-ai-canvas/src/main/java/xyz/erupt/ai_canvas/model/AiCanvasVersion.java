package xyz.erupt.ai_canvas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.MetaModelCreateOnlyVo;

/**
 * One generation round of a view: the user message and the page it produced.
 * Append-only; the designer switches the active version by copying its html
 * back onto the AiCanvas. Managed only through the designer API — not an
 * Erupt-visualized model.
 *
 * @author YuePeng
 * date 2026/8/4
 */
@Table(name = "e_ai_canvas_version")
@Getter
@Setter
@Entity
@NoArgsConstructor
public class AiCanvasVersion extends MetaModelCreateOnlyVo {

    private Long canvasId;

    private Integer version;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String message;

    // Data source and style snapshot of this generation round
    private String dataType;

    private String targetModel;

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
