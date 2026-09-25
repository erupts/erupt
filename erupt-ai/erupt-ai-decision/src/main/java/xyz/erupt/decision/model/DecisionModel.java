package xyz.erupt.decision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.DragSort;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.decision.core.DecisionCore;
import xyz.erupt.decision.handler.DecisionTestButtonHandler;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * Connection to a System One provider. The key stays here and never leaves the server, which
 * is what lets a browser or a downstream service ask for a decision without holding one.
 *
 * @author YuePeng
 */
@Erupt(
        name = "Decision Model", dataProxy = DecisionModelDataProxy.class,
        orderBy = "sort",
        dragSort = @DragSort(field = "sort"),
        rowOperation = @RowOperation(title = "Default Decision Model", icon = "fa fa-wand-magic-sparkles",
                ifExpr = "item.defaultModel !== true",
                mode = RowOperation.Mode.SINGLE, operationHandler = DecisionModelDataProxy.class)
)
@Getter
@Setter
@Table(name = "e_ai_decision_model")
@Entity
@EruptI18n
public class DecisionModel extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Provider"),
            edit = @Edit(title = "Provider", notNull = true, search = @Search,
                    onchange = DecisionModelDataProxy.class,
                    type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = DecisionCore.H.class))
    )
    private String provider;

    @EruptField(
            views = @View(title = "Model"),
            edit = @Edit(title = "Model", notNull = true, desc = "Jev: an alias such as jev-latest, or a pinned version. Laya: auto, english, multilingual or typed-decisions",
                    search = @Search(operator = QueryExpression.LIKE))
    )
    private String model;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "API Domain"),
            // the stored key is posted to whatever this points at, so redirect it in the form only
            edit = @Edit(title = "API Domain", notNull = true, cellEdit = false)
    )
    private String apiUrl;

    @EruptField(
            views = @View(title = "API Key"),
            edit = @Edit(title = "API Key", type = EditType.PASSWORD)
    )
    private String apiKey;

    @Transient
    @EruptField(
            edit = @Edit(title = "Connection Test", type = EditType.BUTTON,
                    buttonType = @ButtonType(icon = "fa fa-bolt", handler = DecisionTestButtonHandler.class))
    )
    private String testModel;

    @EruptField(
            views = @View(title = "Timeout (s)"),
            edit = @Edit(title = "Timeout (s)", notNull = true, type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 120))
    )
    private Integer timeout = 15;

    @EruptField(
            views = @View(title = "Max Retries"),
            edit = @Edit(title = "Max Retries", notNull = true, type = EditType.NUMBER,
                    desc = "Retries with backoff when the provider is rate limited or overloaded",
                    numberType = @NumberType(min = 0, max = 5))
    )
    private Integer retries = 2;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", notNull = true, search = @Search, type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "Active", falseText = "Locked"))
    )
    private Boolean enable = true;

    @EruptField(
            views = @View(title = "Default Decision Model"),
            edit = @Edit(title = "Default Decision Model", show = false,
                    boolType = @BoolType(trueText = "✓", falseText = "×"))
    )
    private Boolean defaultModel = false;

    @EruptField
    private Integer sort;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark", type = ViewType.HTML),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
