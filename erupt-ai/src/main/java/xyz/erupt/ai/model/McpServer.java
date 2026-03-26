package xyz.erupt.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.constants.McpServerType;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/3/24 22:52
 */
@Erupt(
        name = "MCP", dataProxy = McpServerDataProxy.class
)
@Getter
@Setter
@Table(name = "e_ai_mcp_server")
@Entity
@EruptI18n
public class McpServer extends MetaModelUpdateVo implements OnChange<McpServer> {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "状态"),
            edit = @Edit(title = "状态", notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "启用", falseText = "禁用"))
    )
    private Boolean enable = true;

    @Transient
    @EruptField(
            views = @View(title = "Tools")
    )
    private String tools;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "连接协议"),
            edit = @Edit(
                    title = "连接协议", notNull = true, search = @Search,
                    onchange = McpServer.class,
                    type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = McpServerType.H.class)
            )
    )
    private McpServerType serverType = McpServerType.SSE;

    @EruptField(
            views = @View(title = "配置"),
            edit = @Edit(
                    title = "配置", notNull = true, search = @Search,
                    type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json")
            )
    )
    private String config;

    @Override
    public Map<String, Object> populateForm(McpServer mcpServer, String[] params) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> config = new HashMap<>();
        switch (mcpServer.getServerType()) {
            case SSE -> {
                config.put("url", "");
                config.put("headers", new HashMap<>());
            }
            case STDIO -> {
                config.put("command", "");
                config.put("args", new String[]{});
                config.put("env", new HashMap<>());
            }
        }
        data.put(LambdaSee.field(McpServer::getConfig), LLMDataProxy.gson.toJson(config));
        return data;
    }
}
