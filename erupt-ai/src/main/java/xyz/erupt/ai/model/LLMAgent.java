package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
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
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.*;

/**
 * @author YuePeng
 * date 2025/2/22 16:21
 */
@Erupt(
        name = "智能体管理"
)
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

    @ManyToOne
    @EruptField(
            views = @View(title = "大模型", column = "name"),
            edit = @Edit(title = "大模型", type = EditType.REFERENCE_TABLE, notNull = true)
    )
    private LLM llm;

    @EruptField(
            views = @View(title = "提示词动态处理器"),
            edit = @Edit(title = "提示词动态处理器", type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = DynamicPromptFetch.class))
    )
    private String promptHandler;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "提示词"),
            edit = @Edit(title = "提示词", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "python"))
    )
    private String prompt;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述", type = EditType.TEXTAREA)
    )
    private String remark;

}
