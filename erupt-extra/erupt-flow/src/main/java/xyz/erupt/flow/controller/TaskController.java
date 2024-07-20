package xyz.erupt.flow.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.vo.TaskDetailVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.TaskService;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 查询我的任务
     *
     * @return
     */
    @GetMapping("/task/mine")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OaTask>> listMyTasks(String keywords, int pageIndex, int pageSize) {
        return R.ok(taskService.listMyTasks(keywords, pageIndex, pageSize));
    }

    /**
     * 完成任务
     *
     * @return
     */
    @PostMapping("/task/complete/{taskId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> complete(@PathVariable("taskId") Long taskId, @RequestBody JSONObject formContent) {
        taskService.complete(taskId, formContent.getString("remarks"), formContent.getJSONObject("data").toJSONString());
        return R.ok();
    }

    /**
     * 拒绝任务
     *
     * @return
     */
    @PostMapping("/task/refuse/{taskId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> refuse(@PathVariable("taskId") Long taskId, @RequestBody JSONObject formContent) {
        taskService.refuse(taskId, formContent.getString("remarks"), formContent.getJSONObject("data").toJSONString());
        return R.ok();
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/task/detail/{taskId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<TaskDetailVo> getTaskDetail(@PathVariable("taskId") Long taskId) {
        return R.ok(taskService.getTaskDetail(taskId));
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/inst/detail/{instId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<TaskDetailVo> getInstDetail(@PathVariable("instId") Long instId) {
        return R.ok(taskService.getInstDetail(instId));
    }
}
