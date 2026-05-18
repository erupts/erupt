package xyz.erupt.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.ai.core.AiToolboxManager;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptRole;

import java.util.Set;

/**
 * @author YuePeng
 */
@Erupt(name = "AI Role")
@Table(name = "e_ai_role_config")
@Getter
@Setter
@Entity
@EruptI18n
public class LLMRole extends MetaModelUpdateVo {

    @ManyToOne
    @JoinColumn(name = "role_id", unique = true, foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @EruptField(
            views = @View(title = "Role", column = "name"),
            edit = @Edit(title = "Role", notNull = true, type = EditType.REFERENCE_TABLE)
    )
    private EruptRole role;

    @EruptField(
            views = @View(title = "Enabled"),
            edit = @Edit(title = "Enabled", notNull = true, type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Tools"),
            edit = @Edit(title = "Tools", type = EditType.MULTI_CHOICE, multiChoiceType = @MultiChoiceType(fetchHandler = AiToolboxManager.class))
    )
    private Set<String> tools;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "System Prompt"),
            edit = @Edit(title = "System Prompt", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "markdown"))
    )
    private String systemPrompt;

}
