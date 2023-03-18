package xyz.erupt.flow.bean.entity.node;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.erupt.flow.constant.FlowConstant;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaProcessNodeProps {
    /**
     * 条件组
     */
    List<OaProcessNodeGroup> groups;
    /**
     * 条件表达式
     */
    String expression;
    /**
     * 条件之间的关系 AND 和 OR
     */
    String groupsType;

    /**
     * 指定人员 ASSIGN_USER
     * 指定角色 ROLE
     * 发起人自选 SELF_SELECT
     * 发起人自己 SELF
     * 连续多级主管 LEADER_TOP
     * 主管 LEADER
     * 表单内联系人 FORM_USER
     */
    String assignedType = FlowConstant.ASSIGN_TYPE_USER;
    /**
     * 多任务处理方式
     */
    String mode = FlowConstant.COMPLETE_MODE_OR;//默认或签，即任意一个用户完成任务即可
    /**
     * 同意是否需要签字，暂不支持
     */
    boolean sign;
    /**
     * 无人审批时的处理方式
     */
    OaProcessNodeNobody nobody;
    /**
     * 任务超时设置，暂不支持
     */
    JSONObject timeLimit;
    /**
     * 表单权限
     */
    List<OaProcessNodeFormPerms> formPerms;

    /**
     * 发起人自选，暂不支持
     * selfSelect: {
     *   multiple: false
     * }
     */
    JSONObject selfSelect;

    /**
     * 任务处理人，当分配类型为 指定人员时使用
     */
    List<OaProcessNodeAssign> assignedUser;

    /**
     * 任务处理角色，当分配类型为 指定角色时使用
     */
    List<OaProcessNodeAssign> role;

    /**
     * 连续多级主管时使用，如果某一级主管存在多人，则发或签
     */
    OaProcessNodeLeaderTop leaderTop;

    /**
     * 指定一级主管时使用
     */
    OaProcessNodeLeaderTop leader;

    /**
     * 指定表内联系人时使用
     */
    String formUser;

    /**
     * 拒绝配置
     */
    OaProcessNodeRefuse refuse;
}
