package xyz.erupt.flow.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.entity.OaFormGroups;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.ProcessDefinitionService;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 查询所有表单分组
     * 包含流程定义
     * @return
     */
    @GetMapping("/process/groups")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getFormGroups(String keywords){
        List<OaFormGroups> formGroups = processDefinitionService.getFormGroups(OaFormGroups.builder()
                .keywords(keywords)
                .build());
        return EruptApiModel.successApi(formGroups);
    }

    /**
     * 启动流程，会启动当前流程的最新版本
     * @return
     */
    @PostMapping("/process/start/form/{procDefId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel startById(@PathVariable String procDefId, @RequestBody JSONObject formContent) {
        OaProcessInstance processInstance = processDefinitionService.startById(procDefId, formContent.toJSONString());
        return EruptApiModel.successApi(processInstance);
    }

    /**
     * 预览流程时间线
     * @return
     */
    @PostMapping("/process/timeline/preview/{formDefId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel preview(@PathVariable("formDefId") String formDefId, @RequestBody JSONObject formContent) {
        List<OaProcessActivityHistory> activities = processDefinitionService.preview(formDefId, formContent);
        return EruptApiModel.successApi(activities);
    }

    /**
     * 查看流程实例的时间线
     * @return
     */
    @PostMapping("/process/timeline/{instId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel timeline(@PathVariable("instId") Long instId) {
        List<OaProcessActivityHistory> activities = processDefinitionService.preview(instId);
        return EruptApiModel.successApi(activities);
    }
}
