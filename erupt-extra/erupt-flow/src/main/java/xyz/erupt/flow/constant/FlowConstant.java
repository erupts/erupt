package xyz.erupt.flow.constant;

/**
 * @author YuePeng
 * date 2021/1/29 18:25
 */
public class FlowConstant {

    public static final String SERVER_NAME = "erupt-flow";

    public static final String USER_LINK_USERS = "USERS";
    public static final String USER_LINK_ROLES = "ROLES";
    public static final String USER_LINK_CC = "CC";

    public static final String ASSIGN_TYPE_USER = "ASSIGN_USER";//指定人员
    public static final String ASSIGN_TYPE_CC = "CC";//指定抄送人
    public static final String ASSIGN_TYPE_ROLE = "ROLE";//指定角色
    public static final String ASSIGN_TYPE_SELF_SELECT = "SELF_SELECT";//发起人自选，暂时不支持
    public static final String ASSIGN_TYPE_SELF = "SELF";//发起人自己
    public static final String ASSIGN_TYPE_LEADER_TOP = "LEADER_TOP";//连续多级主管
    public static final String ASSIGN_TYPE_LEADER = "LEADER";//指定主管
    public static final String ASSIGN_TYPE_FORM_USER = "FORM_USER";//表单内联系人

    /**
     * 会签以多任务的方式实现
     * 此时每个任务必须是确定的审批人(assignee)，而不能是候选人模式
     * 因为候选人是一个不确定的群体
     */
    public static final String COMPLETE_MODE_NEXT = "NEXT";//会签的执行模式-串行，即生成多个任务，按照顺序依次完成所有
    public static final String COMPLETE_MODE_AND = "AND";//会签的执行模式-并行，生成多个任务，并行完成所有
    public static final String COMPLETE_MODE_OR = "OR";//会签的执行模式-或签，任意一个任务完成即可

    public static final String NOBODY_TO_PASS = "TO_PASS";//无人审批时的处理方式，执行完成
    public static final String NOBODY_TO_REFUSE = "TO_REFUSE";//无人审批时的处理方式，执行驳回
    public static final String NOBODY_TO_ADMIN = "TO_ADMIN";//无人审批时的处理方式，分配给超管用户
    public static final String NOBODY_TO_USER = "TO_USER";//无人审批时的处理方式，分配给指定用户

    public static final String PERM_Readonly = "R";//字段权限，只读模式
    public static final String PERM_Editable = "E";//字段权限，可编辑

    public static final String NODE_ASSIGN_USER = "user";//节点审批人类型-用户
    public static final String NODE_ASSIGN_ROLE = "role";//节点审批人类型-角色

    public static final String REFUSE_TO_END = "TO_END";//驳回规则 TO_END  TO_NODE  TO_BEFORE
    public static final String REFUSE_TO_BEFORE = "TO_BEFORE";
    public static final String REFUSE_TO_NODE = "TO_NODE";

    public static final String NODE_TYPE_ROOT = "ROOT";//节点类型-开始
    public static final String NODE_TYPE_APPROVAL = "APPROVAL";//节点类型-审批
    public static final String NODE_TYPE_CC = "CC";//节点类型-抄送
    public static final String NODE_TYPE_CONDITIONS = "CONDITIONS";//节点类型-条件分支
    public static final String NODE_TYPE_CONDITION = "CONDITION";//节点类型-条件
    public static final String NODE_TYPE_CONCURRENTS = "CONCURRENTS";//节点类型-并行分支
    public static final String NODE_TYPE_CONCURRENT = "CONCURRENT";//节点类型-并行

    public static final String NODE_TYPE_ROOT_VALUE = "root";//节点类型-开始
}
