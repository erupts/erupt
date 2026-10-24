package xyz.erupt.ai_dev.notice;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_dev.constant.DeliveryStatus;
import xyz.erupt.ai_dev.constant.MemberRole;
import xyz.erupt.ai_dev.model.DevProjectMember;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.model.DevRequirementReview;
import xyz.erupt.ai_dev.service.DevReviewService;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.EruptInternalNotice;
import xyz.erupt.notice.model.NoticeScene;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.notice.service.EruptNoticeService;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Sends the in-app notice of a {@link DevNoticeEvent} through erupt-notice. Only wired when that module is
 * on the classpath.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Slf4j
@Component
@ConditionalOnClass(name = "xyz.erupt.notice.service.EruptNoticeService")
public class DevNoticeListener {

    public static final String SCENE_CODE = "ai_dev";

    private static final int PREVIEW_LENGTH = 200;

    @Resource
    private EruptNoticeService eruptNoticeService;

    @Resource
    private EruptInternalNotice eruptInternalNotice;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DevReviewService devReviewService;

    @Resource
    private UpmsTenantSessions upmsTenantSessions;

    private final TransactionTemplate newTransaction;

    public DevNoticeListener(PlatformTransactionManager transactionManager) {
        // after commit the finished transaction is still bound: joining it would write nothing
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // after the change commits; also when it was published outside a transaction
    @TransactionalEventListener(fallbackExecution = true)
    public void on(DevNoticeEvent event) {
        try {
            upmsTenantSessions.runBound(event.tenant(), () -> newTransaction.executeWithoutResult(status -> this.send(event)));
        } catch (Exception e) {
            // a failed notice must not fail the requirement
            log.error("ai-dev notice of requirement {} failed", event.requirementId(), e);
        }
    }

    private void send(DevNoticeEvent event) {
        DevRequirement r = eruptDao.find(DevRequirement.class, event.requirementId());
        if (null == r) return;
        List<Long> receivers = this.receivers(r, event.kind()).stream()
                .filter(Objects::nonNull).filter(uid -> !uid.equals(event.actor())).distinct().toList();
        if (receivers.isEmpty()) return;
        this.ensureScene();
        NoticeMessage message = new NoticeMessage();
        message.setTitle(I18nTranslate.$translate("ai_dev.notice." + event.kind().name().toLowerCase())
                .replace("{0}", r.getCode()).replace("{1}", r.getTitle()));
        String content = StringUtils.defaultIfBlank(this.detail(r, event.kind()),
                I18nTranslate.$translate("ai_dev.notice." + event.kind().name().toLowerCase() + ".body"));
        message.setContent(content.length() > PREVIEW_LENGTH ? content.substring(0, PREVIEW_LENGTH) + "…" : content);
        message.setUrl("/#/ai-dev/pool/" + r.getId());
        message.getParams().put("project", r.getProject().getCode());
        message.getParams().put("requirement", r.getCode());
        message.getParams().put("stage", r.getStage().name());
        eruptNoticeService.send(eruptInternalNotice, SCENE_CODE, receivers, message);
    }

    // what the notice says beyond its title; null falls back to the kind's own sentence
    private String detail(DevRequirement r, DevNoticeEvent.Kind kind) {
        return switch (kind) {
            case SUBMITTED -> r.getDescription();
            // the reviewer's reason or note first, else what was reviewed
            case APPROVED, REJECTED -> devReviewService.latest(r.getId()).map(DevRequirementReview::getComment)
                    .filter(StringUtils::isNotBlank).orElse(r.getDescription());
            case ACCEPTANCE -> r.getVerdict();
            case FAILED -> r.getStatusMessage();
            case DELIVERED -> String.join("\n", eruptDao.getEntityManager().createQuery(
                            "select concat(d.repoAlias, ' → ', d.branch) from DevRequirementDelivery d "
                                    + "where d.requirement.id = :id and d.status = :pushed order by d.id", String.class)
                    .setParameter("id", r.getId()).setParameter("pushed", DeliveryStatus.PUSHED).getResultList());
        };
    }

    private Set<Long> receivers(DevRequirement r, DevNoticeEvent.Kind kind) {
        Long creator = id(r.getCreator()), reviewer = id(r.getReviewer());
        Set<Long> to = new LinkedHashSet<>();
        switch (kind) {
            case SUBMITTED -> {
                if (null != reviewer) to.add(reviewer);
                else r.getProject().getMembers().stream()
                        .filter(m -> m.getRole() == MemberRole.REVIEWER || m.getRole().manages())
                        .map(DevProjectMember::getUser).map(DevNoticeListener::id).forEach(to::add);
            }
            case APPROVED, REJECTED -> to.add(creator);
            default -> Stream.of(creator, reviewer).forEach(to::add);
        }
        return to;
    }

    private void ensureScene() {
        if (null != eruptDao.lambdaQuery(NoticeScene.class).eq(NoticeScene::getCode, SCENE_CODE).one()) return;
        NoticeScene scene = new NoticeScene();
        scene.setCode(SCENE_CODE);
        // persisted once for every user, so a fixed English name rather than the first caller's locale
        scene.setName("AI Dev");
        eruptDao.persistAndFlush(scene);
    }

    private static Long id(EruptUser user) {
        return null == user ? null : user.getId();
    }

}
