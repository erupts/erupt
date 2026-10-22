package xyz.erupt.ai_dev.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.mcp.McpContext;
import xyz.erupt.ai_dev.constant.AgentRole;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.service.DevExecutor;
import xyz.erupt.ai_dev.service.DevTx;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * What a running coding agent calls back on erupt's MCP endpoint: read its requirement, report progress or
 * a QA verdict. Only wired when erupt-ai is on the classpath. Not exposed to chat or user tokens; a run's token ({@link DevMcpGrants}) grants them, and
 * each call acts on that run's requirement only.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@AiToolbox(exposed = false)
@Component
@ConditionalOnClass(name = "xyz.erupt.ai.mcp.McpContext")
public class DevTools {

    public static final String REQUIREMENT = "devRequirement";

    public static final String REPORT_PROGRESS = "devReportProgress";

    public static final String REPORT_VERDICT = "devReportVerdict";

    @Resource
    private DevExecutor devExecutor;

    @Resource
    private DevTx devTx;

    @Resource
    private EruptDao eruptDao;

    @Tool("Read the requirement this run works on: code, title, description and acceptance criteria, repositories and version.")
    public String devRequirement() {
        DevMcpGrants.Grant grant = this.grant(null);
        return devTx.call(grant.tenant(), () -> {
            DevRequirement r = eruptDao.find(DevRequirement.class, grant.requirementId());
            JsonObject json = new JsonObject();
            json.addProperty("code", r.getCode());
            json.addProperty("title", r.getTitle());
            json.addProperty("description", r.getDescription());
            JsonArray repos = new JsonArray();
            r.getRepos().stream().sorted().forEach(repos::add);
            json.add("repositories", repos);
            json.addProperty("version", r.getVersion());
            return json.toString();
        });
    }

    @Tool("Report how far the requirement is. Call it whenever you move to a new phase.")
    public String devReportProgress(@P("Overall progress, 0-100") Integer percent,
                                    @P("Current phase: assess, design, implement or verify") String phase,
                                    @P(value = "One sentence on what was just done", required = false) String note) {
        DevMcpGrants.Grant grant = this.grant(AgentRole.DEVELOPER);
        int value = Math.max(0, Math.min(100, null == percent ? 0 : percent));
        String name = StringUtils.abbreviate(StringUtils.trimToNull(phase), 80);
        devExecutor.reportProgress(grant.tenant(), grant.requirementId(), value, name);
        devExecutor.event(grant.sessionRow(), EventKind.SYSTEM, REPORT_PROGRESS,
                value + "% " + StringUtils.defaultString(name) + (StringUtils.isBlank(note) ? "" : " - " + note.trim()));
        return "Recorded " + value + "%";
    }

    @Tool("Report the QA verdict on the change. Call it once, at the end.")
    public String devReportVerdict(@P("Whether the change meets every acceptance criterion") Boolean pass,
                                   @P("Why it fails, or a one-line summary when it passes") String reason) {
        DevMcpGrants.Grant grant = this.grant(AgentRole.QA);
        boolean ok = Boolean.TRUE.equals(pass);
        String verdict = ((ok ? "PASS " : "FAIL ") + StringUtils.trimToEmpty(reason)).trim();
        grant.verdict(StringUtils.abbreviate(verdict, AnnotationConst.REMARK_LENGTH));
        devExecutor.event(grant.sessionRow(), EventKind.SYSTEM, REPORT_VERDICT, verdict);
        return "Recorded " + (ok ? "PASS" : "FAIL");
    }

    // the run behind this call; the endpoint has already checked the token grants the tool
    private DevMcpGrants.Grant grant(AgentRole role) {
        DevMcpGrants.Grant grant = McpContext.attachment(DevMcpGrants.Grant.class);
        if (null == grant) throw new IllegalStateException("Only a coding agent run can call this tool");
        if (null != role && grant.role() != role) throw new IllegalStateException("Not a tool of the " + grant.role() + " agent");
        return grant;
    }

}
