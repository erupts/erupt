package xyz.erupt.notice.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.AbstractNoticeChannel;
import xyz.erupt.notice.channel.EruptInternalNotice;
import xyz.erupt.notice.model.NoticeLog;
import xyz.erupt.notice.model.NoticeLogDetail;
import xyz.erupt.notice.model.NoticeScene;
import xyz.erupt.notice.model.dataproxy.ChannelChoice;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.notice.service.EruptNoticeService;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

/**
 * Lets an agent tell people what it did. Sending is the same right as adding a row to the notice
 * log by hand, so the tool asks for exactly that: the Notice Log menu and its add power.
 *
 * @author YuePeng
 */
@AiToolbox
@Component
public class NoticeTools {

    private static final String NOTICE_LOG = NoticeLog.class.getSimpleName();

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptNoticeService eruptNoticeService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private ChannelChoice channelChoice;

    @Tool("List what a notice can be sent with: the notice scenes and the channels available right now")
    public String listNoticeOptions() {
        this.requireSend();
        return GsonFactory.getGson().toJson(new Options(
                eruptDao.lambdaQuery(NoticeScene.class).list().stream().map(it -> new Option(it.getCode(), it.getName())).toList(),
                channelChoice.fetch(null).stream().map(it -> new Option(it.getValue(), it.getLabel())).toList()));
    }

    @Tool("Send a notice to users. Returns the notice log id and how many deliveries failed; "
            + "an unknown or disabled user id is refused rather than skipped.")
    public String sendNotice(@P("Notice scene code, from listNoticeOptions") String scene,
                             @P("Ids of the users to notify, from findUsers") List<Long> userIds,
                             @P("Title") String title,
                             @P("Content, plain text") String content,
                             @P(value = "Link opened from the notice", required = false) String url,
                             @P(value = "Channel codes from listNoticeOptions; the in-app message when omitted", required = false) List<String> channels) {
        this.requireSend();
        if (StringUtils.isAnyBlank(title, content)) throw new EruptWebApiRuntimeException("A notice needs a title and a content");
        NoticeScene noticeScene = eruptDao.lambdaQuery(NoticeScene.class).eq(NoticeScene::getCode, scene).one();
        if (null == noticeScene) throw new EruptWebApiRuntimeException("No such notice scene: " + scene);
        List<String> codes = null == channels || channels.isEmpty() ? List.of(EruptInternalNotice.class.getSimpleName()) : channels;
        for (String code : codes) {
            AbstractNoticeChannel channel = AbstractNoticeChannel.getHandlers().get(code);
            if (null == channel || !channel.available()) throw new EruptWebApiRuntimeException("No such notice channel: " + code);
        }
        List<Long> receivers = this.receivers(userIds);
        NoticeMessage message = new NoticeMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setUrl(url);
        eruptNoticeService.send(noticeScene, codes, receivers, message);
        // A channel that fails is recorded on its detail row, not thrown: read it back so the agent hears of it
        long failed = eruptDao.lambdaQuery(NoticeLogDetail.class)
                .eq(NoticeLogDetail::getNoticeLog, eruptDao.find(NoticeLog.class, message.getId()))
                .eq(NoticeLogDetail::getSuccess, false).count();
        return GsonFactory.getGson().toJson(new Sent(message.getId(), receivers.size() * codes.size(), failed));
    }

    // Read through EruptUserVo, which is tenant scoped: a user of another tenant is as unknown as a missing one
    private List<Long> receivers(List<Long> userIds) {
        if (null == userIds || userIds.isEmpty()) throw new EruptWebApiRuntimeException("A notice needs at least one recipient");
        List<Long> distinct = userIds.stream().distinct().toList();
        List<Long> known = eruptDao.lambdaQuery(EruptUserVo.class).in(EruptUserVo::getId, distinct)
                .eq(EruptUserVo::getStatus, true).list().stream().map(EruptUserVo::getId).toList();
        List<Long> unknown = distinct.stream().filter(it -> !known.contains(it)).toList();
        if (!unknown.isEmpty()) throw new EruptWebApiRuntimeException("No such enabled user: " + unknown);
        return known;
    }

    private void requireSend() {
        eruptUserService.requireMenu(NOTICE_LOG);
        // power handlers resolve the model from MetaContext, which on a tool thread still names the chat request
        MetaContext.register(new MetaErupt(NOTICE_LOG, NOTICE_LOG));
        Erupts.powerLegal(PowerInvoke.getPowerObject(EruptCoreService.getErupt(NOTICE_LOG)).isAdd());
    }

    private record Option(String code, String name) {
    }

    private record Options(List<Option> scenes, List<Option> channels) {
    }

    private record Sent(Long noticeLogId, int deliveries, long failed) {
    }

}
