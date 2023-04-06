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
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.flow.service.impl.ProcessInstanceHistoryServiceImpl;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.*;
import java.util.Date;

/**
 * 保存全部的流程实例
 */
@Erupt(name = "流程实例历史表"
        , power = @Power(export = true, add = false, edit = false)
        , dataProxy = ProcessInstanceHistoryServiceImpl.class
        , orderBy = "createDate desc"
)
@Table(name = "oa_hi_process_instance")
@TableName("oa_hi_process_instance")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaProcessInstanceHistory {

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField(views = @View(title = "实例ID"))
    private Long id;

    @EruptField(views = @View(title = "流程定义id"))
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

    @EruptField(views = {
            @View(title = "表单内容", show = false)
    })
    @Column(columnDefinition = "json")//json类型
    private String formItems;

    @Transient//标识虚拟列
    @TableField(exist = false)
    @EruptField(views = @View(title = "详情", type = ViewType.LINK))
    private String detailLink;

    @Transient
    @TableField(exist = false)
    @EruptField(views = @View(title = "类型标识：发起，审批，抄送", export = false))
    private String tag;
}
