package xyz.erupt.flow.process.engine;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.userlink.UserLinkService;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用建造者模式创建任务
 */
@Data
public class TaskBuilder {

    //会签模式 NEXT顺序会签 AND并行会签 OR或签
    private String completeMode;
    //是否激活
    private Boolean active;

    private int sort = 0;

    private OaProcessActivity activity;

    //任务分配用户，每个用户创建一个任务
    private LinkedHashSet<String> users = new LinkedHashSet<>();

    //任务候选角色，创建一个任务，关联到候选人
    private LinkedHashSet<String> linkRoles = new LinkedHashSet<>();

    //任务候选人，创建一个任务，关联到候选人
    private LinkedHashSet<String> linkUsers = new LinkedHashSet<>();

    private UserLinkService userLinkService;

    public TaskBuilder(OaProcessActivity activity) {
        this.activity = activity;
        this.userLinkService = EruptSpringUtil.getBean(UserLinkServiceHolder.class);
    }

    public List<OaTask> build() {
        ArrayList<OaTask> tasks = new ArrayList<>();
        if(FlowConstant.COMPLETE_MODE_OR.equals(this.completeMode)) {//或签，始终只会产生一个任务
            OaTask build = buildPrimeTask();//先把任务生成
            tasks.add(build);
            if(!CollectionUtils.isEmpty(this.users)) {
                if(this.users.size()>1) {//或签情况下，如果分配人大于1，自动转到候选人
                    this.linkUsers.addAll(this.users);
                }else {//否则设置为分配人
                    build.setAssignee(this.users.iterator().next());
                }
            }
            //候选人也可以继续设置，但是有分配人的情况下会优先分配人处理
            ArrayList<OaTaskUserLink> userLinks = new ArrayList<>();
            if(!CollectionUtils.isEmpty(this.linkUsers)) {
                userLinks.addAll(this.linkUsers.stream().map(u -> {
                    OaTaskUserLink link = new OaTaskUserLink();
                    link.setUserLinkType(OaTask.USER_LINK_USERS);
                    link.setUserId(u);
                    return link;
                }).collect(Collectors.toList()));
            }
            if(!CollectionUtils.isEmpty(this.linkRoles)) {
                userLinks.addAll(this.linkRoles.stream().map(u -> {
                    OaTaskUserLink link = new OaTaskUserLink();
                    link.setUserLinkType(OaTask.USER_LINK_ROLES);
                    link.setRoleId(u);
                    return link;
                }).collect(Collectors.toList()));
            }
            build.setUserLinks(userLinks);
        }else {
            /**
             * 注意会签是生成多个任务，所以是不支持候选人的（一任务对应多人）
             * 会把所有候选人变为分配人，然后为每一个分配人生成任务
             */
            if(!CollectionUtils.isEmpty(this.linkUsers)) {
                this.users.addAll(this.linkUsers);//候选人直接转为审批人
            }
            if(!CollectionUtils.isEmpty(this.linkRoles)) {//候选角色则需要查询，然后转为审批人
                LinkedHashSet<OrgTreeVo> userIdsByRoleIds =
                        userLinkService.getUserIdsByRoleIds(this.linkRoles.toArray(new String[this.linkRoles.size()]));
                this.users.addAll(userIdsByRoleIds.stream().map(OrgTreeVo::getId).collect(Collectors.toSet()));
            }
            if(!CollectionUtils.isEmpty(this.users)) {
                int i = 0;
                while (this.users.iterator().hasNext()) {
                    OaTask oaTask = this.buildPrimeTask();
                    oaTask.setAssignee(this.users.iterator().next());
                    if(this.active && i>0) {//当前任务需要激活时，串行只激活第一个，并行激活全部（无需处理）
                        oaTask.setActive(false);
                    }
                    tasks.add(oaTask);
                }
            }
        }
        return tasks;
    }

    /**
     * 添加审批人
     * @return
     */
    public TaskBuilder addUser(String... ids) {
        for (String id : ids) {
            this.users.add(id);
        }
        return this;
    }

    /**
     * 添加候选人
     * @return
     */
    public TaskBuilder addLinkUser(String... ids) {
        for (String id : ids) {
            this.linkUsers.add(id);
        }
        return this;
    }

    /**
     * 添加候选角色
     * @return
     */
    public TaskBuilder addLinkRole(String... ids) {
        for (String id : ids) {
            this.linkRoles.add(id);
        }
        return this;
    }

    /**
     * 自动解析处理人
     * @param users
     */
    public void parseUsers1(List<String> users) {
        if(this.completeMode==null) {
            throw new RuntimeException("必须先指定会签模式");
        }
        if(CollectionUtils.isEmpty(users)) {
            return;
        }
        if(users.size()==1) {//自动变为审批人
            this.users.add(users.get(0));
        }else if(
                FlowConstant.COMPLETE_MODE_AND.equals(this.completeMode)
                || FlowConstant.COMPLETE_MODE_NEXT.equals(this.completeMode)
        ) {//多任务
            this.users.addAll(users);
        }else if(FlowConstant.COMPLETE_MODE_OR.equals(this.completeMode)) {//候选人
            this.linkUsers.addAll(users);
        }
    }

    private OaTask buildPrimeTask() {
        return OaTask.builder()
                .activityId(this.activity.getId())
                .activityKey(this.activity.getActivityKey())
                .executionId(this.activity.getExecutionId())
                .processInstId(this.activity.getProcessInstId())
                .processDefId(this.activity.getProcessDefId())
                .taskName(this.activity.getActivityName())
                .taskDesc(this.activity.getDescription())
                .createDate(new Date())
                .finished(false)
                .completeMode(
                        this.completeMode.equals(FlowConstant.COMPLETE_MODE_AND) && this.completeMode.equals(FlowConstant.COMPLETE_MODE_OR)?
                                OaProcessActivity.PARALLEL:OaProcessActivity.SERIAL
                )
                .completeSort(sort++)
                .active(active)
                .build();
    }
}
