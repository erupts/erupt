package xyz.erupt.flow.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
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
     *
     * @return
     */
    @GetMapping("/process/groups")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OaFormGroups>> getFormGroups(String keywords) {
        return R.ok(processDefinitionService.getFormGroups(OaFormGroups.builder()
                .keywords(keywords)
                .build()));
    }

    /**
     * 启动流程，会启动当前流程的最新版本
     *
     * @return
     */
    @PostMapping("/process/start/form/{procDefId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<OaProcessInstance> startById(@PathVariable String procDefId, @RequestBody JSONObject formContent) {
        return R.ok(processDefinitionService.startById(procDefId, formContent.toJSONString()));
    }

    /**
     * 预览流程时间线
     *
     * @return
     */
    @PostMapping("/process/timeline/preview/{formDefId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OaProcessActivityHistory>> preview(@PathVariable("formDefId") String formDefId, @RequestBody JSONObject formContent) {
        return R.ok(processDefinitionService.preview(formDefId, formContent));
    }

    /**
     * 查看流程实例的时间线
     */
    @PostMapping("/process/timeline/{instId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OaProcessActivityHistory>> timeline(@PathVariable("instId") Long instId) {
        return R.ok(processDefinitionService.preview(instId));
    }
}
