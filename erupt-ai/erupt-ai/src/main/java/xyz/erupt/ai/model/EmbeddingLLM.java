package xyz.erupt.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.handler.EmbeddingLLMDataProxy;
import xyz.erupt.ai.handler.EmbeddingTestButtonHandler;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Erupt(name = "Embedding Model", dataProxy = EmbeddingLLMDataProxy.class)
@Getter
@Setter
@Table(name = "e_ai_embedding_model")
@Entity
@EruptI18n
public class EmbeddingLLM extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name", width = "150px"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Provider"),
            edit = @Edit(
                    title = "Provider",
                    type = EditType.CHOICE,
                    notNull = true,
                    search = @Search,
                    onchange = EmbeddingLLMDataProxy.class,
                    choiceType = @ChoiceType(fetchHandler = EmbeddingCore.H.class)
            )
    )
    private String provider;

    @EruptField(
            views = @View(title = "Model"),
            edit = @Edit(title = "Model", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String model;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "API Domain"),
            edit = @Edit(title = "API Domain")
    )
    private String apiUrl;

    @EruptField(
            views = @View(title = "API Key"),
            edit = @Edit(title = "API Key", type = EditType.PASSWORD)
    )
    private String apiKey;

    @EruptField(
            views = @View(title = "Dimension"),
            edit = @Edit(title = "Dimension", notNull = true,
                    desc = "Vector size the model outputs; collections are created with this size, so it cannot change once documents are embedded")
    )
    private Integer dimension;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", search = @Search, type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    @Transient
    @EruptField(
            edit = @Edit(title = "Test Model", type = EditType.BUTTON,
                    buttonType = @ButtonType(icon = "fa fa-bolt", handler = EmbeddingTestButtonHandler.class))
    )
    private String testModel;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
