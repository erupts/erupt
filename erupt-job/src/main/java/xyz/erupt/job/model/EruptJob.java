package xyz.erupt.job.model;

import lombok.Getter;
import lombok.Setter;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.job.service.ChoiceFetchEruptJobHandler;
import xyz.erupt.job.service.EruptJobService;
import xyz.erupt.upms.model.base.HyperModel;

import javax.annotation.Resource;
import javax.persistence.*;
import java.text.ParseException;
import java.util.List;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@Erupt(
        name = "任务维护",
        dataProxy = EruptJob.class,
        drills = @Drill(code = "list", title = "日志", icon = "fa fa-sliders", link = @Link(linkErupt = EruptJobLog.class, joinColumn = "eruptJob.id")),
        rowOperation = @RowOperation(code = "action", icon = "fa fa-play", title = "执行一次任务", operationHandler = EruptJob.class)
)
@Entity
@Table(name = "e_job", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Component
@Getter
@Setter
public class EruptJob extends HyperModel implements DataProxy<EruptJob>, OperationHandler<EruptJob, Void> {

    @EruptField(
            views = @View(title = "任务编码"),
            edit = @Edit(title = "任务编码", notNull = true, search = @Search)
    )
    private String code;

    @EruptField(
            views = @View(title = "任务名称"),
            edit = @Edit(title = "任务名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "Cron表达式"),
            edit = @Edit(title = "Cron表达式", notNull = true)
    )
    private String cron;

    @EruptField(
            views = @View(title = "JOB处理类"),
            edit = @Edit(title = "JOB处理类", desc = "需实现EruptJobHandler接口",
                    choiceType = @ChoiceType(fetchHandler = ChoiceFetchEruptJobHandler.class)
                    , notNull = true, search = @Search, type = EditType.CHOICE)
    )
    private String handler;

    @EruptField(
            views = @View(title = "任务状态"),
            edit = @Edit(title = "任务状态", boolType = @BoolType(
                    trueText = "启用", falseText = "禁用"
            ), notNull = true, search = @Search)
    )
    private Boolean status;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "失败通知邮箱"),
            edit = @Edit(title = "失败通知邮箱", desc = "使用此功能需配置发信邮箱", type = EditType.TAGS, tagsType = @TagsType)
    )
    private String notifyEmails;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "任务参数"),
            edit = @Edit(title = "任务参数", type = EditType.TEXTAREA)
    )
    private String handlerParam;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述")
    )
    private String remark;

    @Transient
    @Resource
    private EruptJobService eruptJobService;

    @Override
    public void addBehavior(EruptJob eruptJob) {
        eruptJob.setStatus(true);
    }

    @Override
    public void beforeAdd(EruptJob eruptJob) {
        try {
            eruptJobService.modifyJob(eruptJob);
        } catch (SchedulerException | ParseException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public void beforeUpdate(EruptJob eruptJob) {
        this.beforeAdd(eruptJob);
    }

    @Override
    public void beforeDelete(EruptJob eruptJob) {
        try {
            eruptJobService.deleteJob(eruptJob);
        } catch (SchedulerException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public void exec(List<EruptJob> eruptJob, Void param, String[] operationParam) {
        try {
            eruptJobService.triggerJob(eruptJob.get(0));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }
}
