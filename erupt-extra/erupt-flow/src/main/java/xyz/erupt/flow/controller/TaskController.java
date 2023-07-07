package xyz.erupt.flow.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.vo.TaskDetailVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.TaskService;
import xyz.erupt.flow.web.EruptApiPageModel;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 查询我的任务
     * @return
     */
    @GetMapping("/task/mine")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel listMyTasks(String keywords, int pageIndex, int pageSize) {
        PageHelper.startPage(pageIndex, pageSize);//分页
        List<OaTask> page = taskService.listMyTasks(keywords);
        return EruptApiPageModel.successApi(page, pageIndex, pageSize);
    }

    /**
     * 完成任务
     * @return
     */
    @PostMapping("/task/complete/{taskId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel complete(@PathVariable("taskId") Long taskId, @RequestBody JSONObject formContent) {
        taskService.complete(taskId, formContent.getString("remarks"), formContent.getJSONObject("data").toJSONString());
        return EruptApiModel.successApi();
    }

    /**
     * 拒绝任务
     * @return
     */
    @PostMapping("/task/refuse/{taskId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel refuse(@PathVariable("taskId") Long taskId, @RequestBody JSONObject formContent) {
        taskService.refuse(taskId, formContent.getString("remarks"), formContent.getJSONObject("data").toJSONString());
        return EruptApiModel.successApi();
    }

    /**
     * 查询任务详情
     * @return
     */
    @GetMapping("/task/detail/{taskId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getTaskDetail(@PathVariable("taskId") Long taskId) {
        TaskDetailVo taskView = taskService.getTaskDetail(taskId);
        return EruptApiModel.successApi(taskView);
    }

    /**
     * 查询任务详情
     * @return
     */
    @GetMapping("/inst/detail/{instId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getInstDetail(@PathVariable("instId") Long instId) {
        TaskDetailVo taskView = taskService.getInstDetail(instId);
        return EruptApiModel.successApi(taskView);
    }
}
