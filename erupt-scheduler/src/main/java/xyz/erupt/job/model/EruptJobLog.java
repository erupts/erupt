package xyz.erupt.job.model;

import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.*;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
@Erupt(
        orderBy = "startTime desc",
        name = "任务日志",
        power = @Power(export = true, add = false, delete = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "E_JOB_LOG")
@Data
public class EruptJobLog extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "JOB_ID")
    @EruptField(
            views = @View(title = "任务名称", column = "name"),
            edit = @Edit(title = "任务名称", show = false, search = @Search(true), type = EditType.REFERENCE_TREE)
    )
    private EruptJob eruptJob;

    @EruptField(
            views = @View(title = "任务参数")
    )
    private String handlerParam;

    @EruptField(
            views = @View(title = "任务状态"),
            edit = @Edit(title = "任务状态", search = @Search(true))
    )
    private Boolean status;

    @EruptField(
            views = @View(title = "开始时间"),
            edit = @Edit(title = "开始时间", search = @Search(value = true, vague = true),
                    dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date startTime;

    @EruptField(
            views = @View(title = "结束时间")
    )
    private Date endTime;

    @Lob
    @EruptField(
            views = @View(title = "错误信息"),
            edit = @Edit(title = "错误信息")
    )
    private String errorInfo;

}
