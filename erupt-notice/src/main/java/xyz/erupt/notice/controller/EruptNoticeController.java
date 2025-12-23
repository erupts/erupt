package xyz.erupt.notice.controller;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.SimplePage;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.AbstractNoticeChannel;
import xyz.erupt.notice.channel.EruptInternalNotice;
import xyz.erupt.notice.constant.NoticeStatus;
import xyz.erupt.notice.modal.NoticeLogDetail;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/notice")
public class EruptNoticeController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptInternalNotice eruptInternalNotice;

    @EruptLoginAuth
    @GetMapping("/channels")
    public R<List<VLModel>> channels() {
        return R.ok(AbstractNoticeChannel.getHandlers().values().stream()
                .sorted(Comparator.comparingInt(AbstractNoticeChannel::order))
                .map(h -> new VLModel(h.code(), h.name())).toList());
    }

    @EruptLoginAuth
    @GetMapping("/messages")
    public R<SimplePage<NoticeLogDetail>> messages(@RequestParam int page, @RequestParam int size) {
        return R.ok(eruptDao.lambdaQuery(NoticeLogDetail.class)
                .eq(NoticeLogDetail::getChannel, eruptInternalNotice.code())
                .eq(NoticeLogDetail::getSuccess, true)
                .with(NoticeLogDetail::getReceiveUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with()
                .orderByDesc(NoticeLogDetail::getCreateTime).page(size, (page - 1) * size));
    }

    @EruptLoginAuth
    @GetMapping("/message-detail")
    @Transactional
    public R<NoticeLogDetail> message(@RequestParam Long id) {
        NoticeLogDetail noticeLogDetail = eruptDao.lambdaQuery(NoticeLogDetail.class).eq(NoticeLogDetail::getId, id)
                .with(NoticeLogDetail::getReceiveUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with().one();
        if (noticeLogDetail.getStatus() == NoticeStatus.UNREAD) {
            noticeLogDetail.setStatus(NoticeStatus.READ);
            eruptDao.merge(noticeLogDetail);
        }
        return R.ok(noticeLogDetail);
    }

    @EruptLoginAuth
    @GetMapping("/unread-count")
    @Transactional
    public R<Long> unreadCount() {
        return R.ok(eruptDao.lambdaQuery(NoticeLogDetail.class)
                .eq(NoticeLogDetail::getSuccess, true)
                .eq(NoticeLogDetail::getStatus, NoticeStatus.UNREAD)
                .with(NoticeLogDetail::getReceiveUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).count());
    }

    @EruptLoginAuth
    @GetMapping("/read-all")
    @Transactional
    public R<Void> readAll() {
        List<NoticeLogDetail> noticeLogDetails = eruptDao.lambdaQuery(NoticeLogDetail.class)
                .eq(NoticeLogDetail::getSuccess, true)
                .eq(NoticeLogDetail::getStatus, NoticeStatus.UNREAD)
                .with(NoticeLogDetail::getReceiveUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).list();
        noticeLogDetails.forEach(detail -> detail.setStatus(NoticeStatus.READ));
        return R.ok();
    }

}
