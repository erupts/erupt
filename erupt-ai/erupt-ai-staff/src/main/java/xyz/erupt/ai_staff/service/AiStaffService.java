package xyz.erupt.ai_staff.service;

import com.google.gson.JsonObject;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai_staff.channel.StaffChannel;
import xyz.erupt.ai_staff.model.AiStaff;
import xyz.erupt.ai_staff.model.AiStaffTask;
import xyz.erupt.ai_staff.model.AiStaffTaskLog;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Runs a staff task: impersonates the staff's bound account, lets the LLM work
 * with the tools that account is allowed to use, and files the work report.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@Service
@Slf4j
public class AiStaffService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptTokenService eruptTokenService;

    @Async
    public void executeAsync(Long taskId) {
        this.execute(taskId);
    }

    public void execute(Long taskId) {
        AiStaffTask task = eruptDao.find(AiStaffTask.class, taskId);
        if (null == task || !Boolean.TRUE.equals(task.getEnable())) return;
        AiStaff staff = task.getStaff();
        if (null == staff || !Boolean.TRUE.equals(staff.getEnable())) return;
        AiStaffTaskLog taskLog = new AiStaffTaskLog();
        taskLog.setTaskId(task.getId());
        taskLog.setStaffName(staff.getName());
        taskLog.setStartTime(new Date());
        try {
            taskLog.setReport(this.chat(staff, task.getInstruction()));
            taskLog.setStatus(true);
            this.pushReport(task, taskLog.getReport());
        } catch (Exception e) {
            log.error("AI staff task [{}] failed", task.getName(), e);
            taskLog.setStatus(false);
            taskLog.setErrorInfo(ExceptionUtils.getStackTrace(e));
        } finally {
            taskLog.setEndTime(new Date());
            eruptDao.persistAndFlush(taskLog);
            MetaContext.remove();
        }
    }

    /**
     * One working turn as the given staff: impersonates the bound account,
     * runs the LLM with the tools that account may use, returns the answer.
     * Callers are responsible for {@code MetaContext.remove()} afterwards.
     */
    public String chat(AiStaff staff, String userMessage) {
        LLM llm = null != staff.getLlm() ? staff.getLlm() : eruptDao.lambdaQuery(LLM.class)
                .eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one();
        if (null == llm) throw new EruptWebApiRuntimeException("Not found LLM config");
        LlmRequest llmRequest = llm.toLlmRequest();
        llmRequest.setAutoCallTool(true);
        llmRequest.setAgentPrompt(this.buildStaffPrompt(staff));
        // Work under the staff's bound account so AI role prompts and tool permissions apply
        EruptUser user = staff.getEruptUser();
        MetaContext.register(new MetaUser(user.getId(), user.getAccount(), user.getName()));
        // AI tools authenticate via the session token, not MetaUser alone —
        // open an ephemeral session for the bound account and close it after the turn
        String token = Erupts.generateCode(24);
        eruptTokenService.loginToken(user, token);
        MetaContext.registerToken(token);
        try {
            return LlmCore.getLLM(llm).chat(llmRequest, new ArrayList<>(List.of(UserMessage.from(userMessage))));
        } finally {
            eruptTokenService.logoutToken(user.getAccount(), token);
        }
    }

    // Report push failure must not fail the task: the work itself already succeeded
    private void pushReport(AiStaffTask task, String report) {
        if (null == task.getChannel() || !Boolean.TRUE.equals(task.getChannel().getEnable())) return;
        try {
            StaffChannel.get(task.getChannel().getType()).push(
                    GsonFactory.getGson().fromJson(task.getChannel().getConfig(), JsonObject.class), report);
        } catch (Exception e) {
            log.warn("AI staff task [{}] report push failed: {}", task.getName(), e.getMessage());
        }
    }

    private String buildStaffPrompt(AiStaff staff) {
        StringBuilder prompt = new StringBuilder("You are '").append(staff.getName()).append("', an AI staff member");
        if (StringUtils.isNotBlank(staff.getPosition())) {
            prompt.append(" working as ").append(staff.getPosition());
        }
        prompt.append(" in this organization.\n\n");
        if (StringUtils.isNotBlank(staff.getDuty())) {
            prompt.append("## Duty\n").append(staff.getDuty()).append("\n\n");
        }
        prompt.append("""
                ## Working Rules
                - You work autonomously: no human is available to answer questions, make reasonable decisions yourself.
                - Use the available tools to complete the assigned task.
                - When finished, output a concise work report in Markdown summarizing what was done and the results.
                """);
        return prompt.toString();
    }

}
