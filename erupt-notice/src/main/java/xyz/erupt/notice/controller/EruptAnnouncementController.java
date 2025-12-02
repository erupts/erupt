package xyz.erupt.notice.controller;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.SimplePage;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.constant.AnnouncementStatus;
import xyz.erupt.notice.modal.EruptUserAnnouncement;
import xyz.erupt.notice.modal.NoticeAnnouncement;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/announcement")
public class EruptAnnouncementController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @EruptLoginAuth
    @GetMapping("/list")
    @Transactional
    public R<SimplePage<NoticeAnnouncement>> announcements(@RequestParam int page, @RequestParam int size) {
        return R.ok(eruptDao.lambdaQuery(NoticeAnnouncement.class)
                .eq(NoticeAnnouncement::getStatus, AnnouncementStatus.OPEN)
                .orderByDesc(NoticeAnnouncement::getCreateTime)
                .page(size, (page - 1) * size));
    }

    @EruptLoginAuth
    @GetMapping("/popups")
    public R<List<NoticeAnnouncement>> popups() {
        EruptUserAnnouncement eruptUserAnn = eruptDao.find(EruptUserAnnouncement.class, eruptUserService.getCurrentUid());
        return R.ok(eruptDao.lambdaQuery(NoticeAnnouncement.class)
                .eq(NoticeAnnouncement::getStatus, AnnouncementStatus.OPEN)
                .gt(NoticeAnnouncement::getCreateTime, DateUtils.addDays(new Date(), -7))
                .gt(null != eruptUserAnn.getAnnReadId(), NoticeAnnouncement::getId, eruptUserAnn.getAnnReadId())
                .orderByDesc(NoticeAnnouncement::getCreateTime)
                .list());
    }

    @Transactional
    @GetMapping("/mark-read/{annId}")
    @EruptLoginAuth
    public R<Void> markRead(@PathVariable Long annId) {
        EruptUserAnnouncement eruptUserAnn = eruptDao.find(EruptUserAnnouncement.class, eruptUserService.getCurrentUid());
        eruptUserAnn.setAnnReadId(annId);
        eruptDao.merge(eruptUserAnn);
        return R.ok();
    }

}
