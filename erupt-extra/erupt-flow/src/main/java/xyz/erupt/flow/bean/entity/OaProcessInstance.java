package xyz.erupt.flow.bean.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.service.impl.ProcessInstanceServiceImpl;

import javax.persistence.*;
import java.util.Date;

/**
 * 流程实例，每次发起工单都会产生新的流程实例
 * 只保存运行中的流程，流程结束后会被删除
 */
@Erupt(name = "流程实例"
        , power = @Power(export = true, add = false, edit = false)
        , dataProxy = ProcessInstanceServiceImpl.class
)
@Table(name = "oa_ru_process_instance")
@TableName("oa_ru_process_instance")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaProcessInstance {

    //RUNNING:运行中 PAUSE:暂停 FINISHED:结束 SHUTDOWN:中止
    public final static String RUNNING = "RUNNING";
    public final static String PAUSE = "PAUSE";
    public final static String FINISHED = "FINISHED";
    public final static String SHUTDOWN = "SHUTDOWN";

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField
    @TableId(type = IdType.AUTO)
    private Long id;

    @EruptField(views = @View(title = "流程定义id", show = true))
    private String processDefId;

    @EruptField(views = @View(title = "表单id", show = false))
    private Long formId;

    @EruptField(views = @View(title = "表单名称")
            , edit = @Edit(title = "表单名称", search = @Search(vague = true))
    )
    private String formName;

    @EruptField(views = @View(title = "业务主键")
            , edit = @Edit(title = "业务主键", search = @Search)
    )
    private String businessKey;

    @EruptField(views = @View(title = "业务标题")
            , edit = @Edit(title = "业务标题", search = @Search(vague = true))
    )
    private String businessTitle;

    @EruptField(views = @View(title = "状态", desc = "RUNNING:运行中 PAUSE:暂停 FINISHED:结束 SHUTDOWN:中止")
            , edit = @Edit(
                    title = "状态", search = @Search, type = EditType.CHOICE
                    , choiceType = @ChoiceType(
                        vl = {
                                @VL(label = "运行中", value = "RUNNING"),
                                @VL(label = "暂停", value = "PAUSE"),
                                @VL(label = "结束", value = "FINISHED"),
                                @VL(label = "中止", value = "SHUTDOWN", disable = true),
                        })
            )
    )
    private String status;

    @EruptField(views = @View(title = "发起人ID", show = false))
    private String creator;

    @EruptField(views = @View(title = "发起人")
            , edit = @Edit(title = "发起人", search = @Search(vague = true))
    )
    private String creatorName;

    @EruptField(views = @View(title = "发起时间", type = ViewType.DATE_TIME)
            , edit = @Edit(title = "发起时间", search = @Search(vague = true))
    )
    private Date createDate;

    @EruptField(views = @View(title = "结束时间", type = ViewType.DATE_TIME)
            , edit = @Edit(title = "结束时间", search = @Search(vague = true))
    )
    private Date finishDate;

    @EruptField(views = @View(title = "结束原因"))
    private String reason;

    @EruptField(views = {
            @View(title = "表单内容", show = false)
    })
    @Lob
    @Column//json类型
    private String formItems;

    @Lob
    @Column//json类型
    private String process;

    public JSONObject getFormContent() {
        return JSON.parseObject(this.formItems);
    }

    public OaProcessNode getProcessNode() {
        return JSON.parseObject(this.process, OaProcessNode.class);
    }
}
