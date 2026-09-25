package xyz.erupt.comment.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.comment.model.EruptRecordComment;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.EruptInternalNotice;
import xyz.erupt.notice.model.NoticeScene;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.notice.service.EruptNoticeService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

/**
 * Turns the @mentions of a comment into internal notices through the erupt-notice module.
 * Only wired when that module is on the classpath; without it mentions are stored and
 * highlighted but nobody is notified.
 */
@Slf4j
@Component
@ConditionalOnClass(name = "xyz.erupt.notice.service.EruptNoticeService")
public class CommentMentionNotifier {

    public static final String SCENE_CODE = "comment_mention";

    private static final int PREVIEW_LENGTH = 200;

    @Resource
    private EruptNoticeService eruptNoticeService;

    @Resource
    private EruptInternalNotice eruptInternalNotice;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @Transactional
    public void notify(EruptRecordComment comment, List<Long> receivers) {
        if (null == receivers || receivers.isEmpty()) return;
        try {
            this.ensureScene();
            NoticeMessage message = new NoticeMessage();
            message.setTitle(I18nTranslate.$translate("comment.mention.title")
                    .replace("{0}", eruptUserService.getSimpleUserInfo().getUsername())
                    .replace("{1}", modelTitle(comment.getErupt())));
            String content = comment.getContent();
            message.setContent(content.length() > PREVIEW_LENGTH ? content.substring(0, PREVIEW_LENGTH) + "…" : content);
            // the table under the fill layout (no admin shell) with the record opened as a deep link
            message.setUrl("/#/fill/build/table/" + comment.getErupt() + "?id=" + comment.getRecordId());
            message.getParams().put("erupt", comment.getErupt());
            message.getParams().put("recordId", comment.getRecordId());
            message.getParams().put("commentId", comment.getId());
            eruptNoticeService.send(eruptInternalNotice, SCENE_CODE, receivers, message);
        } catch (Exception e) {
            // a failed notice must not fail the comment
            log.error("comment mention notice failed", e);
        }
    }

    private void ensureScene() {
        if (null != eruptDao.lambdaQuery(NoticeScene.class).eq(NoticeScene::getCode, SCENE_CODE).one()) return;
        NoticeScene scene = new NoticeScene();
        scene.setCode(SCENE_CODE);
        // persisted once for every user, so a fixed English name rather than the first caller's locale
        scene.setName("Comment Mention");
        eruptDao.persistAndFlush(scene);
    }

    private static String modelTitle(String eruptName) {
        EruptModel model = EruptCoreService.getErupt(eruptName);
        return null == model ? eruptName : I18nTranslate.$translate(model.getErupt().name());
    }

}
