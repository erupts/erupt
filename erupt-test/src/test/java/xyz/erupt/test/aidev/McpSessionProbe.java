package xyz.erupt.test.aidev;

import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A tool that, like erupt-ai-claw's, needs the caller's session: it answers who erupt thinks is calling.
 */
@AiToolbox
@Component
public class McpSessionProbe {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptDataController eruptDataController;

    // what erupt-ai-claw's findEruptDataByPk does: one record through erupt's own data API and its powers
    @Tool("Reads one EruptUser record by id")
    public String mcpReadUser(Long id) {
        MetaContext.register(new MetaErupt("EruptUser", "EruptUser"));
        return String.valueOf(eruptDataController.getEruptDataById("EruptUser", String.valueOf(id)).get("account"));
    }

    public record Filter(String field, String value) {
    }

    // a structured argument, like eruptDataQuery's conditions: its schema must say array of objects
    @Tool("Echoes filters back as field=value pairs")
    public String mcpEchoFilters(List<Filter> filters) {
        return null == filters ? "none" : filters.stream().map(f -> f.field() + "=" + f.value()).collect(Collectors.joining(","));
    }

    @Tool("Answers the account of the erupt session the call runs in")
    public String mcpSessionProbe() {
        MetaUserinfo user = eruptUserService.getSimpleUserInfo();
        return null == user ? "anonymous" : user.getAccount();
    }

}
