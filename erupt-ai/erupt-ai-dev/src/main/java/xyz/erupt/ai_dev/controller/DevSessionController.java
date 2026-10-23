package xyz.erupt.ai_dev.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.constant.SessionStatus;
import xyz.erupt.ai_dev.model.DevAgentEvent;
import xyz.erupt.ai_dev.model.DevAgentSession;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI dev agent sessions
 *
 * @order 60
 * @apiNote Follows an agent session live: poll the log with the last seq seen.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai-dev/session")
public class DevSessionController {

    private static final int PAGE = 500;

    public record Event(Integer seq, EventKind kind, String name, String content, LocalDateTime time) {
    }

    public record Log(SessionStatus status, List<Event> events) {
    }

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DevAccessService devAccessService;

    /**
     * Log lines of a session after a given seq, at most 500 per call, with the session's status.
     *
     * @param id    session id
     * @param after last seq already seen, 0 for the start
     */
    @GetMapping("/{id}/events")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public Log events(@PathVariable("id") Long id, @RequestParam(value = "after", defaultValue = "0") int after) {
        DevAgentSession session = eruptDao.find(DevAgentSession.class, id);
        if (null == session) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_not_found"));
        devAccessService.requireMember(session.getProject().getId());
        List<Event> events = eruptDao.getEntityManager().createQuery(
                        "select e from DevAgentEvent e where e.session.id = :id and e.seq > :after order by e.seq", DevAgentEvent.class)
                .setParameter("id", id).setParameter("after", after).setMaxResults(PAGE).getResultList().stream()
                .map(e -> new Event(e.getSeq(), e.getKind(), e.getName(), e.getContent(), e.getCreatedAt())).toList();
        return new Log(session.getStatus(), events);
    }

}
