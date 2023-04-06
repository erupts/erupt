package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Erupt(name = "任务操作日志"
        , power = @Power(export = true, add = false, edit = false)
        , orderBy = "operationDate desc"
)
@Table(name = "oa_hi_task_operation")
@TableName("oa_hi_task_operation")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaTaskOperation extends BaseModel {

    public static final String COMPLETE = "COMPLETE";//完成
    public static final String REFUSE = "REFUSE";//拒绝，走拒绝策略
    public static final String JUMP = "JUMP";//跳转，指定节点进行跳转
    public static final String SHUTDOWN = "SHUTDOWN";//终止
    public static final String ASSIGN = "ASSIGN";//转办

    @EruptField(views = @View(title = "流程实例id"))
    private Long processInstId;

    @EruptField(views = @View(title = "流程定义id"))
    private String processDefId;

    @EruptField(views = @View(title = "任务ID")
            , edit = @Edit(title = "任务ID", search = @Search)
    )
    private Long taskId;

    @EruptField(views = @View(title = "任务名"))
    private String taskName;

    @EruptField(views = @View(title = "操作人ID")
            , edit = @Edit(title = "操作人ID", search = @Search)
    )
    private String operator;

    @EruptField(views = @View(title = "操作人")
            , edit = @Edit(title = "操作人", search = @Search(vague = true))
    )
    private String operatorName;

    @EruptField(views = @View(title = "操作", desc = "COMPLETE 完成;REJECT 拒绝;BACK 退回;JUMP 跳转;SHUTDOWN 终止;"))
    private String operation;

    @EruptField(views = @View(title = "操作说明"))
    private String remarks;

    @EruptField(views = @View(title = "目标节点ID", desc = "拒绝，退回，跳转三种情况，才有目标节点"))
    private String targetNodeId;

    @EruptField(views = @View(title = "目标节点"))
    private String targetNodeName;

    @EruptField(views = @View(title = "操作时间", type = ViewType.DATE_TIME)
            , edit = @Edit(title = "操作时间", search = @Search(vague = true))
    )
    private Date operationDate;
}
