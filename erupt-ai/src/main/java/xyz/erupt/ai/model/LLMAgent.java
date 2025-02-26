package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.Entity;
import javax.persistence.Table;

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
            edit = @Edit(title = "名称")
    )
    private String name;

    @EruptField(
            views = @View(title = "绑定模型", column = "name"),
            edit = @Edit(title = "绑定模型", type = EditType.REFERENCE_TABLE)
    )
    private LLM llm;

    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述")
    )
    private String description;

}
