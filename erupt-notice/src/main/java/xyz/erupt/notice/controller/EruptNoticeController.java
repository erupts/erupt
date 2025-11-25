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
import xyz.erupt.jpa.dao.EruptLambdaQuery;
import xyz.erupt.notice.channel.AbstractNoticeChannel;
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

    @EruptLoginAuth
    @GetMapping("/channels")
    public R<List<VLModel>> channels() {
        return R.ok(AbstractNoticeChannel.getHandlers().values().stream()
                .sorted(Comparator.comparingInt(AbstractNoticeChannel::order))
                .map(h -> new VLModel(h.code(), h.name())).toList());
    }

    @EruptLoginAuth
    @GetMapping("/messages")
    public R<SimplePage<NoticeLogDetail>> messages(@RequestParam String channel, @RequestParam int page, @RequestParam int size) {
        SimplePage<NoticeLogDetail> simplePage = new SimplePage<>();
        EruptLambdaQuery<NoticeLogDetail> eruptLambdaQuery = eruptDao.lambdaQuery(NoticeLogDetail.class)
                .eq(NoticeLogDetail::getChannel, channel)
                .eq(NoticeLogDetail::getSuccess, true)
                .with(NoticeLogDetail::getReceiveUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with();
        simplePage.setTotal(eruptLambdaQuery.count());
        simplePage.setList(eruptLambdaQuery.orderByDesc(NoticeLogDetail::getCreateTime).offset((page - 1) * size).limit(size).list());
        return R.ok(simplePage);
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

}
