package xyz.erupt.print.controller;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.print.model.EruptPrintConfig;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/print/config")
public class EruptPrintConfigController {

    @Resource
    private EruptDao eruptDao;

    @GetMapping("/{erupt}/list")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<List<EruptPrintConfig>> list(@PathVariable String erupt) {
        return R.ok(eruptDao.lambdaQuery(EruptPrintConfig.class)
                .eq(EruptPrintConfig::getErupt, erupt).list());
    }

    @PostMapping("/{erupt}/add")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public R<Void> add(@PathVariable String erupt, @RequestBody EruptPrintConfig printConfig) {
        eruptDao.persist(printConfig);
        return R.ok();
    }

    @PostMapping("/{erupt}/update")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public R<Void> update(@PathVariable String erupt, @RequestParam Long id, @RequestBody EruptPrintConfig printConfig) {
        eruptDao.merge(printConfig);
        return R.ok();
    }

    @PostMapping("/{erupt}/delete")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public R<Void> delete(@PathVariable String erupt, @RequestParam Long id) {
        eruptDao.lambdaQuery(EruptPrintConfig.class)
                .eq(EruptPrintConfig::getId, id)
                .eq(EruptPrintConfig::getErupt, erupt).delete();
        return R.ok();
    }

}
