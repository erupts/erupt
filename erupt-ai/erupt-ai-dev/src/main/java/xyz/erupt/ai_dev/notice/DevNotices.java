package xyz.erupt.ai_dev.notice;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;

/**
 * Announces stage changes; {@link DevNoticeListener} turns them into notices when erupt-notice is present,
 * and without it they go nowhere.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Component
public class DevNotices {

    @Resource
    private ApplicationEventPublisher publisher;

    // done by the current user, who is not told about it
    public void publish(DevRequirement requirement, DevNoticeEvent.Kind kind) {
        MetaUser user = MetaContext.getUser();
        this.publish(requirement, kind, null == user ? null : user.getUid());
    }

    // done by the executor: its threads may carry the context of whoever woke them, which must not mute anyone
    public void publishFromExecutor(DevRequirement requirement, DevNoticeEvent.Kind kind) {
        this.publish(requirement, kind, null);
    }

    private void publish(DevRequirement requirement, DevNoticeEvent.Kind kind, Long actor) {
        publisher.publishEvent(new DevNoticeEvent(requirement.getProject().getTenantId(), requirement.getId(), kind, actor));
    }

}
