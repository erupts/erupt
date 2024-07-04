package xyz.erupt.job.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.job.model.data_proxy.EruptJobDataProcess;
import xyz.erupt.job.model.data_proxy.EruptJobExecDialog;
import xyz.erupt.job.model.data_proxy.NotifyEmailRender;
import xyz.erupt.job.service.EruptJobFetch;
import xyz.erupt.job.service.JobMessageListener;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.toolkit.notify.RedisNotifyDataProxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        name = "任务配置",
        dataProxy = EruptJobDataProcess.class,
        drills = @Drill(title = "日志", icon = "fa fa-sliders", link = @Link(linkErupt = EruptJobLog.class, joinColumn = "jobId")),
        rowOperation = @RowOperation(code = "action", icon = "fa fa-play", title = "执行一次任务", mode = RowOperation.Mode.SINGLE, eruptClass = EruptJobExecDialog.class, operationHandler = EruptJobDataProcess.class)
)
@PreDataProxy(value = RedisNotifyDataProxy.class, params = JobMessageListener.JOB_TOPIC)
@Entity
@Table(name = "e_job", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Component
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class EruptJob extends MetaModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "编码", width = "100px"),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true), readonly = @Readonly(add = false))
    )
    private String code;

    @EruptField(
            views = @View(title = "任务执行器"),
            edit = @Edit(title = "任务执行器", desc = "实现EruptJobHandler接口即可",
                    choiceType = @ChoiceType(fetchHandler = EruptJobFetch.class, trigger = EruptJobFetch.class)
                    , notNull = true, search = @Search, type = EditType.CHOICE)
    )
    private String handler;

    @EruptField(
            views = @View(title = "任务名称"),
            edit = @Edit(title = "任务名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "Cron表达式", width = "150px"),
            edit = @Edit(title = "Cron表达式", notNull = true)
    )
    private String cron;

    @EruptField(
            views = @View(title = "任务状态"),
            edit = @Edit(title = "任务状态", boolType = @BoolType(
                    trueText = "启用", falseText = "禁用"
            ), notNull = true, search = @Search)
    )
    private Boolean status = true;

    @EruptField(
            views = @View(title = "记录日志"),
            edit = @Edit(title = "记录日志", boolType = @BoolType(
                    trueText = "是", falseText = "否"
            ), notNull = true, search = @Search)
    )
    private Boolean recordLog = true;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "失败通知邮箱", ifRender = @ExprBool(exprHandler = NotifyEmailRender.class)),
            edit = @Edit(title = "失败通知邮箱", ifRender = @ExprBool(exprHandler = NotifyEmailRender.class), type = EditType.TAGS, tagsType = @TagsType)
    )
    private String notifyEmails;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "任务参数"),
            edit = @Edit(title = "任务参数", type = EditType.CODE_EDITOR)
    )
    private String handlerParam;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述")
    )
    private String remark;


}
