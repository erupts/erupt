package xyz.erupt.flow.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.core.service.EruptFlowCoreService;
import xyz.erupt.flow.core.view.EruptFormModel;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class EruptFlowFormController {


    /**
     * 查询所有已加载的表单类
     */
    @GetMapping("/forms")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<EruptFormModel>> getEruptForms() {
        return R.ok(EruptFlowCoreService.getEruptForms());
    }
}
