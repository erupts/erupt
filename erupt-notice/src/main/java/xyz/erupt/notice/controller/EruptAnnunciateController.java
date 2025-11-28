package xyz.erupt.notice.controller;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.modal.Annunciate;
import xyz.erupt.upms.annotation.EruptLoginAuth;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/annunciate")
public class EruptAnnunciateController {

    @Resource
    private EruptDao eruptDao;

    @EruptLoginAuth
    @GetMapping("/read")
    @Transactional
    public R<Void> read() {
        eruptDao.lambdaQuery(Annunciate.class)
                .eq(Annunciate::getTitle, true);
        return R.ok();
    }

}
