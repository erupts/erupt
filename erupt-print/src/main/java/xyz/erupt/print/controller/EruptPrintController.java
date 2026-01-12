package xyz.erupt.print.controller;

import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/print")
public class EruptPrintController {

    @PostMapping("/{erupt}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<String> print(@PathVariable String erupt, @PathVariable String id, @RequestBody String content) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Object data = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id));
        AtomicReference<String> contentReference = new AtomicReference<>(content);
        DataProxyInvoke.invoke(eruptModel, dataProxy -> contentReference.set(dataProxy.print(data, contentReference.get())));
        return R.ok(contentReference.get());
    }

}
