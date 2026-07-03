package xyz.erupt.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.constants.McpServerType;
import xyz.erupt.ai.handler.RefreshMcpServerHandler;
import xyz.erupt.ai.service.McpServerService;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2026/3/24 22:52
 */
@Erupt(
        name = "MCP",
        dataProxy = McpServerService.class,
        rowOperation = @RowOperation(
                title = "Refresh",
                icon = "fa fa-refresh",
                mode = RowOperation.Mode.SINGLE,
                operationHandler = RefreshMcpServerHandler.class
        )
)
@Getter
@Setter
@Table(name = "e_ai_mcp_server")
@Entity
@EruptI18n
public class McpServer extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status", notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "Enable", falseText = "Disable"))
    )
    private Boolean enable = true;

    @Transient
    @EruptField(
            views = @View(title = "Tools", width = "500px")
    )
    private String tools;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Connection Protocol"),
            edit = @Edit(
                    title = "Connection Protocol", notNull = true, search = @Search,
                    onchange = McpServerService.class,
                    type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = McpServerType.H.class)
            )
    )
    private McpServerType serverType = McpServerType.SSE;

    @EruptField(
            views = @View(title = "Config"),
            edit = @Edit(
                    title = "Config", notNull = true,
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json")
            )
    )
    private String config;

}
