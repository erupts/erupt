package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.flow.service.impl.TaskHistoryServiceImpl;

import javax.persistence.*;
import java.util.Date;

/**
 * 保存所有的任务
 */
@Erupt(name = "历史任务"
        , power = @Power(export = true, add = false, edit = false)
        , orderBy = "createDate desc"
        , dataProxy = TaskHistoryServiceImpl.class
)
@Table(name = "oa_hi_task")
@TableName("oa_hi_task")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaTaskHistory {

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField(views = @View(title = "任务ID"))
    private Long id;

    @Transient//标识虚拟列
    @TableField(exist = false)
    @EruptField(views = @View(title = "流程名称")
            , edit = @Edit(title = "流程名称", search = @Search(vague = true))
    )
    private String formName;

    @Transient
    @TableField(exist = false)
    @EruptField(views = @View(title = "类型标识：发起，审批，抄送", export = false))
    private String tag;

    @EruptField(views = @View(title = "任务名")
            , edit = @Edit(title = "任务名", search = @Search(vague = true))
    )
    private String taskName;

    @EruptField(views = @View(title = "任务类型：root开始节点，userTask用户任务，cc抄送"))
    private String taskType;

    @EruptField(views = @View(title = "实例ID")
            , edit = @Edit(title = "实例ID", type = EditType.NUMBER, search = @Search())
    )
    private Long processInstId;

    @EruptField(views = @View(title = "节点id"))
    private Long activityId;

    @EruptField(views = @View(title = "节点key"))
    private String activityKey;

    @EruptField(views = @View(title = "线程id", show = false))
    private Long executionId;

    @EruptField(views = @View(title = "所属人", desc = "任务持有人，最优先处理此任务"))
    private String taskOwner;//任务持有人，最优先处理此任务，优先级为1

    @EruptField(views = @View(title = "分配人", desc = "任务没有所属人时，分配人优先处理此任务"))
    private String assignee;//任务指定人，没有持有人时，指定人处理此任务，优先级为2

    @Transient//标识虚拟列
    @TableField(exist = false)
    @EruptField(views = @View(title = "候选人", desc = "所属人和分配人为空时，由候选人处理任务"))
    private String links;//激活状态，只有激活的任务可以被完成

    @EruptField(views = @View(title = "是否激活", desc = "只有激活的任务可以被完成")
            , edit = @Edit(title = "是否激活", search = @Search)
    )
    private Boolean active;//激活状态，只有激活的任务可以被完成

    @Transient//标识虚拟列
    @TableField(exist = false)
    @EruptField(views = @View(title = "业务标题")
            , edit = @Edit(title = "业务标题", search = @Search(vague = true))
    )
    private String businessTitle;

    @EruptField(views = @View(title = "流程定义id", show = false))
    private String processDefId;

    @EruptField(views = @View(title = "创建时间", type = ViewType.DATE_TIME)
            , edit = @Edit(title = "创建时间", search = @Search(vague = true))
    )
    private Date createDate;//创建时间

    @EruptField(views = @View(title = "申领时间", desc = "一个任务只能被一个用户申领，申领后成为该任务持有人", type = ViewType.DATE_TIME))
    private Date claimDate;//申领日期，一个任务只能被一个用户申领，申领后成为该任务持有人

    @EruptField(views = @View(title = "是否完成")
            , edit = @Edit(title = "是否完成", search = @Search)
    )
    private Boolean finished;

    @EruptField(views = @View(title = "完成用户ID")
            , edit = @Edit(title = "完成用户ID", search = @Search)
    )
    private String finishUser;

    @EruptField(views = @View(title = "完成用户")
            , edit = @Edit(title = "完成用户", search = @Search(vague = true))
    )
    private String finishUserName;

    @EruptField(views = @View(title = "完成时间", type = ViewType.DATE_TIME))
    private Date finishDate;

    @EruptField(views = @View(title = "任务说明"))
    private String taskDesc;

    /**
     * 任务执行模式
     * SERIAL 串行，即按照顺序，一个执行完之后，另一个任务才可以被执行，直到所有任务完成线程继续
     * PARALLEL 并行，即同一个线程（processExecutionId）下的任务，可以同时被执行，所有任务完成，线程继续
     */
    @EruptField(views = @View(title = "任务执行模式", desc = "SERIAL 串行; PARALLEL 并行;"))
    private String completeMode;

    @EruptField(views = @View(title = "执行顺序", desc = "串行模式下按照顺序完成任务"))
    private Integer completeSort;//完成顺序，单任务和并行不需要关注顺序。串行模式下，每个任务执行完之后，取当前线程下顺序最小的一个任务执行
}
