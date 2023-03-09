package xyz.erupt.flow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.ProcessInstanceService;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @GetMapping("/data/OaProcessInstance/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getById(@PathVariable("id") Long id) {
        OaProcessInstance byId = processInstanceService.getById(id);
        return EruptApiModel.successApi(byId);
    }
}
