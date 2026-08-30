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
 * Append-only and immutable; AiCanvas points at rows here via its active and
 * publish version ids. The data model is not snapshotted — it is fixed on the
 * canvas itself. Managed only through the designer API — not an
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

    // Style snapshot of this generation round; restored when the version is re-activated
    private String style;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String html;

    public AiCanvasVersion(AiCanvas view, Integer version, String message, String html) {
        this.canvasId = view.getId();
        this.version = version;
        this.message = message;
        this.html = html;
        this.style = view.getStyle();
    }

}
