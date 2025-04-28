package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author YuePeng
 * date 2025/2/22 16:21
 */
@Erupt(
        name = "大语言模型", dataProxy = LLMDataProxy.class,
        orderBy = "sort",
        rowOperation = {
                @RowOperation(title = "对话测试", icon = "fa fa-comments",
                        tpl = @Tpl(path = "/tpl/llm-chat.ftl", height = "85vh", tplHandler = LLMDataProxy.class),
                        mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL),
                @RowOperation(title = "默认对话模型", icon = "fa fa-magic",
                        ifExpr = "item.defaultLLM == '×'",
                        mode = RowOperation.Mode.SINGLE, operationHandler = LLMDataProxy.class)
        }
)
@Getter
@Setter
@Table(name = "e_ai_llm")
@Entity
@EruptI18n
public class LLM extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "模型名称"),
            edit = @Edit(title = "模型名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "大语言模型"),
            edit = @Edit(
                    title = "大语言模型",
                    type = EditType.CHOICE,
                    notNull = true,
                    search = @Search,
                    choiceType = @ChoiceType(fetchHandler = LlmCore.H.class, trigger = LLMDataProxy.class)
            )
    )
    private String llm;

    @EruptField(
            views = @View(title = "模型版本"),
            edit = @Edit(title = "模型版本", notNull = true, search = @Search(vague = true))
    )
    private String model;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "API 域名"),
            edit = @Edit(title = "API 域名", notNull = true)
    )
    private String apiUrl;

    @EruptField(
            views = @View(title = "API Key", type = ViewType.HTML, width = "80px"),
            edit = @Edit(title = "API Key")
    )
    private String apiKey;

    @EruptField(
            views = @View(title = "上下文记忆轮次"),
            edit = @Edit(title = "上下文记忆轮次", notNull = true,
                    type = EditType.SLIDER, sliderType = @SliderType(max = 100))
    )
    private Integer maxContext = 20;

    @EruptField(
            views = @View(title = "状态", sortable = true),
            edit = @Edit(
                    title = "状态", search = @Search,
                    type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "激活", falseText = "锁定")
            )
    )
    private Boolean enable = true;

    @EruptField(
            views = @View(title = "默认对话模型"),
            edit = @Edit(title = "默认对话模型", show = false, boolType = @BoolType(trueText = "✓", falseText = "×"))
    )
    private Boolean defaultLLM = false;

    @EruptField(
            views = @View(title = "使用顺序", sortable = true),
            edit = @Edit(title = "使用顺序", notNull = true)
    )
    private Integer sort = 0;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "模型配置"),
            edit = @Edit(title = "模型配置", type = EditType.CODE_EDITOR, notNull = true,
                    codeEditType = @CodeEditorType(language = "json")
            )
    )
    @Ref(LlmConfig.class)
    private String config;


    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "备注", type = ViewType.HTML),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
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
