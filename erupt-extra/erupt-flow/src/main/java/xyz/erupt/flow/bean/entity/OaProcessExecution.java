package xyz.erupt.flow.bean.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;

import javax.persistence.*;
import java.util.Date;

/**
 * 流程的执行线程
 * 通常情况下一个流程实例只有一个线程在执行
 * 当有并行节点时，会产生多个子线程。所有子线程结束后，才能回到主线程继续执行
 */
@Erupt(name = "线程"
        , power = @Power(export = true, add = false, edit = false)
)
@Table(name = "oa_ru_process_execution")
@TableName("oa_ru_process_execution")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaProcessExecution {

    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_ENDED = "ENDED";

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField
    @TableId(type = IdType.AUTO)
    private Long id;

    @EruptField(views = @View(title = "父线程id"))
    private Long parentId;

    @EruptField(views = @View(title = "流程实例id"))
    private Long processInstId;

    @EruptField(views = @View(title = "流程定义id"))
    private String processDefId;

    @EruptField(views = @View(title = "启动线程的节点id"))
    private String startNodeId;

    @EruptField(views = @View(title = "启动线程的节点名称"))
    private String startNodeName;

    @EruptField(views = @View(title = "状态", desc = "RUNNING 运行中; WAITING 等待子线程合并; ENDED 结束;")
            , edit = @Edit(title = "状态", search = @Search)
    )
    private String status;

    @EruptField(views = @View(title = "创建时间", type = ViewType.DATE_TIME))
    private Date created;

    @EruptField(views = @View(title = "更新时间", type = ViewType.DATE_TIME))
    private Date updated;

    @EruptField(views = @View(title = "结束时间", type = ViewType.DATE_TIME))
    private Date ended;

    @EruptField(views = @View(title = "当前节点", show = false))
    @Lob
    @Column//json类型
    private String process;

    @EruptField(views = @View(title = "结束原因", show = false))
    private String reason;

    public OaProcessNode getProcessNode() {
        if(this.getProcess()==null) {
            return null;
        }
        return JSON.parseObject(this.getProcess(), OaProcessNode.class);
    }
}
