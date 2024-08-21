package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeNobody;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeProps;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.listener.AfterCreateTaskListener;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.TaskService;
import xyz.erupt.flow.service.TaskUserLinkService;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务激活后，如果无人处理则会触发
 */
@Component
public class AfterActiveTaskImpl implements AfterCreateTaskListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private ProcessActivityService processActivityService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserLinkServiceHolder userLinkService;
    @Autowired
    private TaskUserLinkService taskUserLinkService;

    @Override
    public void execute(OaTask task) {
        if(!task.getActive()) {
            return;
        }
        //判断任务是否有人可以处理
        if(
                task.getTaskOwner()==null//没有所属人
                        && task.getAssignee()==null//没有分配人
                        && taskUserLinkService.countUsersByTaskId(task.getId())<=0//候选人
        ) {//触发无人审批事件
            OaProcessActivity activity = processActivityService.getById(task.getActivityId());
            OaProcessNodeProps props = activity.getProcessNode().getProps();
            OaProcessNodeNobody nobodyConf = props.getNobody();
            if(nobodyConf==null) {//未配置无人审批策略
                throw new EruptApiErrorTip("无人处理的审批方式为空");
            }
            if(FlowConstant.NOBODY_TO_PASS.equals(nobodyConf.getHandler())) {
                //直接完成
                taskService.complete(task.getId(), null, "无人处理，自动完成", null, null);
            }else if(FlowConstant.NOBODY_TO_REFUSE.equals(nobodyConf.getHandler())) {
                //直接拒绝
                taskService.refuse(task.getId(), "无人处理，自动拒绝", null);
            }else if(FlowConstant.NOBODY_TO_ADMIN.equals(nobodyConf.getHandler())) {//分配给超管用户
                LinkedHashSet<OrgTreeVo> userIds = userLinkService.getAdminUsers();
                if(CollectionUtils.isEmpty(userIds)) {
                    throw new EruptApiErrorTip("未查询到超管用户");
                }
                //将任务转办给超管
                taskService.assign(task.getId(), userIds, "无人处理，转办给超管用户");
            }else if(FlowConstant.NOBODY_TO_USER.equals(nobodyConf.getHandler())) {
                //将任务转办给指定用户
                Set<OrgTreeVo> users =
                        nobodyConf.getAssignedUser().stream().map(au -> OrgTreeVo.builder()
                                .id(au.getId())
                                .name(au.getName())
                                .build()
                        ).collect(Collectors.toSet());
                taskService.assign(task.getId(), users, "无人处理，转办给指定用户");
            }else {
                throw new EruptApiErrorTip("请设置无人处理的审批方式");
            }
        }
    }
}
