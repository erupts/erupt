package xyz.erupt.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import java.util.Date;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        orderBy = "startTime desc",
        name = "任务日志",
        power = @Power(export = true, add = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_job_log")
@Getter
@Setter
public class EruptJobLog extends BaseModel {

    @EruptField(
            views = @View(title = "任务名称", type = ViewType.TEXT),
            edit = @Edit(title = "任务名称", search = @Search, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SqlChoiceFetchHandler.class, fetchHandlerParams = "select id, name from e_job"))
    )
    private Long jobId;

    @Column(length = AnnotationConst.REMARK_LENGTH)
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
            views = @View(title = "开始时间", type = ViewType.DATE_TIME),
            edit = @Edit(title = "任务执行时间", search = @Search(vague = true))
    )
    private Date startTime;

    @EruptField(
            views = @View(title = "结束时间", type = ViewType.DATE_TIME)
    )
    private Date endTime;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "执行结果"),
            edit = @Edit(title = "执行结果")
    )
    private String resultInfo;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "错误信息", type = ViewType.HTML, sortable = true),
            edit = @Edit(title = "错误信息")
    )
    private String errorInfo;

}
