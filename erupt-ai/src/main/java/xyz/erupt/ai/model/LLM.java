package xyz.erupt.ai.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.ChoiceTrigger;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.linq.lambda.LambdaSee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/2/22 16:21
 */
@Erupt(name = "大模型管理")
@Getter
@Setter
@Table(name = "e_ai_llm")
@Entity
@EruptI18n
public class LLM extends MetaModelUpdateVo implements ChoiceTrigger {

    @EruptField(
            views = @View(title = "模型名称"),
            edit = @Edit(title = "模型名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "大语言模型"),
            edit = @Edit(
                    title = "大语言模型",
                    type = EditType.CHOICE,
                    notNull = true,
                    choiceType = @ChoiceType(fetchHandler = SuperLLM.H.class, trigger = LLM.class)
            )
    )
    private String llm;

    @EruptField(
            views = @View(title = "模型版本"),
            edit = @Edit(title = "模型版本", notNull = true)
    )
    private String model;

    @EruptField(
            views = @View(title = "生成随机性"),
            edit = @Edit(title = "生成随机性", desc = "temperature", numberType = @NumberType(min = 0, max = 1))
    )
    private Float temperature;

    @EruptField(
            views = @View(title = "top_p"),
            edit = @Edit(title = "top_p", numberType = @NumberType(min = 0, max = 1))
    )
    private Float topP;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "模型配置"),
            edit = @Edit(title = "模型配置", type = EditType.CODE_EDITOR, notNull = true,
                    codeEditType = @CodeEditorType(language = "json")
            )
    )
    private String config;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "备注", type = ViewType.HTML),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    private Long promptTokens;

    private Long completionTokens;

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Map<String, Object> trigger(Object code, String[] params) {
        if (null != code && !"null".equals(code)) {
            Map<String, Object> ret = new HashMap<>();
            ret.put(LambdaSee.field(LLM::getModel), SuperLLM.getLLM(code.toString()).model());
            ret.put(LambdaSee.field(LLM::getConfig), gson.toJson(SuperLLM.getLLM(code.toString()).config()));
            return ret;
        }
        return Collections.emptyMap();
    }

}
