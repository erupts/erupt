package xyz.erupt.job.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.*;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@Erupt(
        orderBy = "startTime desc",
        name = "任务日志",
        power = @Power(export = true, add = false, delete = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_job_log")
@Getter
@Setter
public class EruptJobLog extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "job_id")
    @EruptField(
            views = @View(title = "任务名称", column = "name"),
            edit = @Edit(title = "任务名称", show = false, search = @Search, type = EditType.REFERENCE_TREE)
    )
    private EruptJob eruptJob;

    @EruptField(
            views = @View(title = "任务参数")
    )
    private String handlerParam;

    @EruptField(
            views = @View(title = "任务状态"),
            edit = @Edit(title = "任务状态", boolType = @BoolType(trueText = "成功", falseText = "失败"), search = @Search)
    )
    private Boolean status;

    @EruptField(
            views = @View(title = "开始时间")
    )
    private Date startTime;

    @EruptField(
            views = @View(title = "结束时间")
    )
    private Date endTime;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "执行结果"),
            edit = @Edit(title = "执行结果")
    )
    private String resultInfo;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "错误信息", type = ViewType.HTML),
            edit = @Edit(title = "错误信息")
    )
    private String errorInfo;

}
