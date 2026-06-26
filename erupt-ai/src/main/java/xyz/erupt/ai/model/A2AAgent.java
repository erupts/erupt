package xyz.erupt.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.handler.RefreshA2AAgentHandler;
import xyz.erupt.ai.service.A2AAgentService;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * A2A (Agent-to-Agent) remote agent configuration
 *
 * @author YuePeng
 * date 2026/5/15
 */
@Erupt(
        name = "A2A",
        dataProxy = A2AAgentService.class,
        rowOperation = @RowOperation(
                title = "Refresh",
                icon = "fa fa-refresh",
                mode = RowOperation.Mode.SINGLE,
                operationHandler = RefreshA2AAgentHandler.class
        )
)
@Getter
@Setter
@Table(name = "e_ai_a2a_agent")
@Entity
@EruptI18n
public class A2AAgent extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status", notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "Agent URL", notNull = true, desc = "A2A agent base URL")
    )
    private String agentUrl;

    @Transient
    @EruptField(
            views = @View(title = "Skills", width = "500px")
    )
    private String skills;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Headers"),
            edit = @Edit(title = "Headers", type = EditType.CODE_EDITOR,
                    desc = "Additional HTTP request headers in JSON format, e.g. {\"X-Custom\": \"value\"}",
                    codeEditType = @CodeEditorType(language = "json"))
    )
    private String headers;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description", type = EditType.TEXTAREA)
    )
    private String remark;

}
