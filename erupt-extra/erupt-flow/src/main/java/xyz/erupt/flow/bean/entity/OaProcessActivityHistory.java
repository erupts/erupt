package xyz.erupt.flow.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Erupt(name = "节点历史"
        , power = @Power(export = true, add = false, edit = false)
)
@Table(name = "oa_hi_process_activity")
@TableName("oa_hi_process_activity")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaProcessActivityHistory extends BaseModel {

    @EruptField(views = @View(title = "节点key"))
    private String activityKey;

    @EruptField(views = @View(title = "节点名")
            , edit = @Edit(title = "节点名", search = @Search(vague = true))
    )
    private String activityName;

    @EruptField(views = @View(title = "说明"))
    private String description;

    @EruptField(views = @View(title = "线程id"))
    private Long executionId;

    @EruptField(views = @View(title = "流程实例id"))
    private Long processInstId;

    @EruptField(views = @View(title = "流程定义id"))
    private String processDefId;

    /**
     * 任务完成条件
     *
     */
    private String completeCondition;

    /**
     * 任务执行模式
     * SERIAL 串行，即按照顺序，一个执行完之后，另一个任务才可以被执行，直到所有任务完成线程继续
     * PARALLEL 并行，即同一个节点下的任务，可以同时被执行
     */
    @EruptField(views = @View(title = "任务执行模式", desc = "SINGLE 单任务; SERIAL 串行; PARALLEL 并行;"))
    private String completeMode;

    @EruptField(views = @View(title = "是否激活")
            , edit = @Edit(title = "是否激活", search = @Search)
    )
    private Boolean active;

    @EruptField(views = @View(title = "创建时间", type = ViewType.DATE_TIME)
            , edit = @Edit(title = "创建时间", search = @Search(vague = true))
    )
    private Date createDate;//创建时间

    @EruptField(views = @View(title = "是否完成")
            , edit = @Edit(title = "是否完成", search = @Search)
    )
    private Boolean finished;

    @EruptField(views = @View(title = "完成时间", type = ViewType.DATE_TIME))
    private Date finishDate;

    @Transient
    @TableField(exist = false)
    private List<OaTaskHistory> tasks;
}
