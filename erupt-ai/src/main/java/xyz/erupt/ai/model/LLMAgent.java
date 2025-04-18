package xyz.erupt.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Types;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import xyz.erupt.ai.handler.DynamicPromptFetch;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * @author YuePeng
 * date 2025/2/22 16:21
 */
@Erupt(name = "智能体")
@Table(name = "e_ai_llm_agent")
@Getter
@Setter
@Entity
@EruptI18n
public class LLMAgent extends MetaModelUpdateVo {

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
            views = @View(title = "提示词动态处理器"),
            edit = @Edit(title = "提示词动态处理器", type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = DynamicPromptFetch.class))
    )
    @JsonIgnore
    private String promptHandler;

    @EruptField(
            views = @View(title = "生成随机性"),
            edit = @Edit(title = "生成随机性", desc = "temperature", numberType = @NumberType(min = 0, max = 1))
    )
    @JsonIgnore
    private Float temperature;

    @EruptField(
            views = @View(title = "top_p"),
            edit = @Edit(title = "top_p", numberType = @NumberType(min = 0, max = 1))
    )
    @JsonIgnore
    private Float topP;

//    @EruptField(
//            views = @View(title = "候选词列表"),
//            edit = @Edit(title = "候选词列表", type = EditType.TAGS)
//    )
//    @Column(length = AnnotationConst.REMARK_LENGTH)
//    private String hint;

    @Lob
    @JsonIgnore
    @JdbcTypeCode(Types.LONGVARCHAR)
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

}
