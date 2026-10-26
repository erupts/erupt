package xyz.erupt.ai_dev.proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai_dev.constant.RequirementStage;
import xyz.erupt.ai_dev.constant.ReviewDecision;
import xyz.erupt.ai_dev.model.DevInstructionForm;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.model.DevReviewForm;
import xyz.erupt.ai_dev.notice.DevNoticeEvent;
import xyz.erupt.ai_dev.notice.DevNotices;
import xyz.erupt.ai_dev.runner.ClaudeCodeRunner;
import xyz.erupt.ai_dev.runner.DevAgentProcess;
import xyz.erupt.ai_dev.runner.DevRunners;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.ai_dev.service.DevDeliveryService;
import xyz.erupt.ai_dev.service.DevExecutor;
import xyz.erupt.ai_dev.service.DevRunContext;
import xyz.erupt.ai_dev.service.DevReviewService;
import xyz.erupt.ai_dev.service.DevWorkspaceService;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserVo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * The requirement pool's actions. Each checks the stage it starts from and who may take it: any member
 * submits, retries, stops and cancels; reviewers, owners and maintainers approve, reject, request changes
 * and accept.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Component
public class DevRequirementOperation implements OperationHandler<DevRequirement, Object> {

    @Resource
    private DevAccessService devAccessService;

    @Resource
    private DevExecutor devExecutor;

    @Resource
    private DevAgentProcess devAgentProcess;

    @Resource
    private DevRunners devRunners;

    @Resource
    private DevDeliveryService devDeliveryService;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DevNotices devNotices;

    @Resource
    private DevWorkspaceService devWorkspaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private DevReviewService devReviewService;

    @Override
    public String exec(List<DevRequirement> data, Object form, String[] param) {
        String action = param[0];
        // retry and rework carry an instruction for the agent, approve and reject the reviewer's comment
        String instruction = StringUtils.trimToNull(form instanceof DevInstructionForm f ? f.getInstruction()
                : form instanceof DevReviewForm f ? f.getComment() : null);
        boolean queued = false;
        String message = null;
        for (DevRequirement row : data) {
            String result = transactionTemplate.execute(status -> this.apply(action, eruptDao.find(DevRequirement.class, row.getId()), instruction));
            if (null != result) message = result;
            queued |= Set.of("approve", "retry", "rework").contains(action);
        }
        // the worker reads the queue after this commit
        if (queued) devExecutor.kick();
        if (null != message) throw new EruptWebApiRuntimeException(message);
        return null;
    }

    private String apply(String action, DevRequirement r, String instruction) {
        Long project = r.getProject().getId();
        devAccessService.requireMember(project);
        switch (action) {
            case "submit" -> {
                this.from(r, RequirementStage.DRAFT);
                r.setStage(RequirementStage.REVIEW);
                devNotices.publish(r, DevNoticeEvent.Kind.SUBMITTED);
            }
            case "approve" -> {
                this.from(r, RequirementStage.REVIEW);
                devAccessService.requireApprover(r);
                EruptUser approver = eruptDao.find(EruptUser.class, devAccessService.user().getId());
                if (null == r.getReviewer()) r.setReviewer(eruptDao.find(EruptUserVo.class, approver.getId()));
                devReviewService.record(r, ReviewDecision.APPROVED, instruction, approver);
                // the reviewer's note goes to the run
                this.queue(r, null == instruction ? null : "Reviewer's note: " + instruction);
                devNotices.publish(r, DevNoticeEvent.Kind.APPROVED);
            }
            case "reject" -> {
                this.from(r, RequirementStage.REVIEW);
                devAccessService.requireApprover(r);
                if (null == instruction) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.reject_reason_required"));
                devReviewService.record(r, ReviewDecision.REJECTED, instruction, eruptDao.find(EruptUser.class, devAccessService.user().getId()));
                r.setStage(RequirementStage.DRAFT);
                devNotices.publish(r, DevNoticeEvent.Kind.REJECTED);
            }
            case "retry" -> {
                this.from(r, RequirementStage.FAILED);
                this.queue(r, instruction);
            }
            case "rework" -> {
                this.from(r, RequirementStage.ACCEPTANCE);
                devAccessService.requireReviewer(project);
                if (null == instruction) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.instruction_required"));
                this.queue(r, instruction);
            }
            case "stop" -> {
                this.from(r, RequirementStage.RUNNING, RequirementStage.VERIFYING);
                if (!devAgentProcess.stop(r.getId())) {
                    throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.not_running_here"));
                }
            }
            case "deliver" -> {
                this.from(r, RequirementStage.ACCEPTANCE);
                devAccessService.requireReviewer(project);
                return devDeliveryService.deliver(r);
            }
            case "cancel" -> {
                if (Set.of(RequirementStage.DELIVERED, RequirementStage.CANCELLED, RequirementStage.RUNNING, RequirementStage.VERIFYING)
                        .contains(r.getStage())) {
                    throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.stage_invalid"));
                }
                r.setStage(RequirementStage.CANCELLED);
                // nothing will run from it again: its worktrees go with it
                this.discard(r);
            }
            case "cleanup" -> {
                // agents never work from it at this point; acceptance still needs the change to deliver it
                this.from(r, RequirementStage.DRAFT, RequirementStage.REVIEW, RequirementStage.FAILED, RequirementStage.CANCELLED);
                this.discard(r);
            }
            default -> throw new EruptWebApiRuntimeException("Unknown action " + action);
        }
        return null;
    }

    private void discard(DevRequirement r) {
        try {
            devWorkspaceService.discard(DevRunContext.of(r.getProject(), r));
        } catch (IOException | RuntimeException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.cleanup_failed") + " " + e.getMessage());
        }
    }

    // only queues what this instance can run: the requirement's CLI installed
    private void queue(DevRequirement r, String instruction) {
        String executor = StringUtils.defaultIfBlank(r.getExecutor(), ClaudeCodeRunner.CODE);
        if (devRunners.find(executor).filter(devRunners::installed).isEmpty()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.no_executor") + ": " + executor);
        }
        r.setStage(RequirementStage.QUEUED);
        r.setInstruction(instruction);
        r.setStatusMessage(null);
        r.setProgress(0);
        r.setPhase(null);
    }

    private void from(DevRequirement r, RequirementStage... stages) {
        if (!List.of(stages).contains(r.getStage())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.stage_invalid") + ": " + r.getStage());
        }
    }

}
