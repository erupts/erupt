package xyz.erupt.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.core.annotation.Ref;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2025/2/22 16:21
 */
@Erupt(
        name = "LLM", dataProxy = LLMDataProxy.class,
        orderBy = "sort",
        rowOperation = {
                @RowOperation(title = "Model Test", icon = "fa fa-comments",
                        tpl = @Tpl(path = "/tpl/ai-chat.ftl", height = "85vh"),
                        mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL),
                @RowOperation(title = "Default Chat Model", icon = "fa fa-magic",
                        ifExpr = "item.defaultLLM === '×'",
                        mode = RowOperation.Mode.SINGLE, operationHandler = LLMDataProxy.class)
        },
        layout = @Layout(tableLeftFixed = 1)
)
@Getter
@Setter
@Table(name = "e_ai_llm")
@Entity
@EruptI18n
public class LLM extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Model Name",width = "150px"),
            edit = @Edit(title = "Model Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Model Provider"),
            edit = @Edit(
                    title = "Model Provider",
                    type = EditType.CHOICE,
                    notNull = true,
                    search = @Search,
                    onchange = LLMDataProxy.class,
                    choiceType = @ChoiceType(fetchHandler = LlmCore.H.class)
            )
    )
    private String llm;

    @EruptField(
            views = @View(title = "Model"),
            edit = @Edit(title = "Model", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String model;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "API Domain"),
            edit = @Edit(title = "API Domain", notNull = true)
    )
    private String apiUrl;

    @EruptField(
            edit = @Edit(title = "API Key")
    )
    private String apiKey;

    @Transient
    @EruptField(
            edit = @Edit(title = "Advanced", type = EditType.GROUP,
                    groupType = @GroupType(fields = {"maxContext", "config", "remark"}, collapsed = true)
            )
    )
    private String advancedGroup;

    @EruptField(
            views = @View(title = "Context Memory Rounds"),
            edit = @Edit(title = "Context Memory Rounds", notNull = true,
                    type = EditType.SLIDER, sliderType = @SliderType(max = 100))
    )
    private Integer maxContext = 20;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(
                    title = "Status", search = @Search,
                    type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "Active", falseText = "Locked")
            )
    )
    private Boolean enable = true;

    @EruptField(
            views = @View(title = "Default Chat Model"),
            edit = @Edit(title = "Default Chat Model", show = false, boolType = @BoolType(trueText = "✓", falseText = "×"))
    )
    private Boolean defaultLLM = false;

    @EruptField(
            views = @View(title = "Usage Order", sortable = true),
            edit = @Edit(title = "Usage Order", notNull = true)
    )
    private Integer sort = 0;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Model Config"),
            edit = @Edit(title = "Model Config", type = EditType.CODE_EDITOR, notNull = true,
                    codeEditType = @CodeEditorType(language = "json")
            )
    )
    @Ref(LlmConfig.class)
    private String config;


    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark", type = ViewType.HTML),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;


    public LlmRequest toLlmRequest() {
        LlmConfig llmConfig = GsonFactory.getGson().fromJson(config, LlmConfig.class);
        LlmRequest llmRequest = llmConfig.toLlmRequest();
        llmRequest.setUrl(apiUrl);
        llmRequest.setApiKey(apiKey);
        llmRequest.setModel(model);
        return llmRequest;
    }

}
