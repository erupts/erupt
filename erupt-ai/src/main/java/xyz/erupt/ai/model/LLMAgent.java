package xyz.erupt.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.handler.DynamicPromptFetch;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
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
@Erupt(name = "智能体", dataProxy = LLMAgent.class)
@Table(name = "e_ai_llm_agent")
@Getter
@Setter
@Entity
@EruptI18n
public class LLMAgent extends MetaModelUpdateVo implements DataProxy<LLMAgent> {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "是否启用"),
            edit = @Edit(title = "是否启用", notNull = true)
    )
    private Boolean enable = true;

    @EruptField(
            views = @View(title = "提示词处理器"),
            edit = @Edit(title = "提示词处理器", type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = DynamicPromptFetch.class))
    )
    @JsonIgnore
    private String promptHandler;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "智能体参数"),
            edit = @Edit(title = "智能体参数", type = EditType.CODE_EDITOR, notNull = true,
                    codeEditType = @CodeEditorType(language = "json")
            )
    )
    @JsonIgnore
    @Ref(LlmConfig.class)
    private String config;

//    @EruptField(
//            views = @View(title = "候选词列表"),
//            edit = @Edit(title = "候选词列表", type = EditType.TAGS)
//    )
//    @Column(length = AnnotationConst.REMARK_LENGTH)
//    private String hint;

    @Lob
    @JsonIgnore
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "提示词"),
            edit = @Edit(title = "提示词", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "python"))
    )
    private String prompt;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "能力描述"),
            edit = @Edit(title = "能力描述", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public void addBehavior(LLMAgent llmAgent) {
        llmAgent.setConfig(LLMDataProxy.gson.toJson(new LlmConfig()));
    }

    public void mergeToLLmRequest(LLM llm) {
        LlmRequest llmRequest = llm.toLlmRequest();
        LlmConfig llmConfig = GsonFactory.getGson().fromJson(config, LlmConfig.class);
        BeanUtils.copyProperties(llmConfig, llmRequest);
    }

}
