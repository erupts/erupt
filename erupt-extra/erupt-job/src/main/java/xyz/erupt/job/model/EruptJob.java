package xyz.erupt.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        name = "Job Config",
        dataProxy = EruptJobDataProcess.class,
        drills = @Drill(title = "Log", icon = "fa fa-sliders", link = @Link(linkErupt = EruptJobLog.class, joinColumn = "jobId")),
        rowOperation = @RowOperation(code = "action", icon = "fa fa-play", title = "Execute Once", mode = RowOperation.Mode.SINGLE, eruptClass = EruptJobExecDialog.class, operationHandler = EruptJobDataProcess.class)
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
            views = @View(title = "Code", width = "100px"),
            edit = @Edit(title = "Code", notNull = true, search = @Search(vague = true), readonly = @Readonly(add = false))
    )
    private String code;

    @EruptField(
            views = @View(title = "Job Handler"),
            edit = @Edit(title = "Job Handler", desc = "Implement EruptJobHandler interface",
                    choiceType = @ChoiceType(fetchHandler = EruptJobFetch.class)
                    , onchange = EruptJobFetch.class
                    , notNull = true, search = @Search, type = EditType.CHOICE)
    )
    private String handler;

    @EruptField(
            views = @View(title = "Job Name"),
            edit = @Edit(title = "Job Name", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "Cron Expression", width = "150px"),
            edit = @Edit(title = "Cron Expression", notNull = true)
    )
    private String cron;

    @EruptField(
            views = @View(title = "Job Status"),
            edit = @Edit(title = "Job Status", boolType = @BoolType(
                    trueText = "Enable", falseText = "Disable"
            ), notNull = true, search = @Search)
    )
    private Boolean status = true;

    @EruptField(
            views = @View(title = "Record Log"),
            edit = @Edit(title = "Record Log", boolType = @BoolType(
                    trueText = "Yes", falseText = "No"
            ), notNull = true, search = @Search)
    )
    private Boolean recordLog = true;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Failure Notify Email", ifRender = @ExprBool(exprHandler = NotifyEmailRender.class)),
            edit = @Edit(title = "Failure Notify Email", ifRender = @ExprBool(exprHandler = NotifyEmailRender.class), type = EditType.TAGS, tagsType = @TagsType)
    )
    private String notifyEmails;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Job Params"),
            edit = @Edit(title = "Job Params", type = EditType.CODE_EDITOR)
    )
    private String handlerParam;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description")
    )
    private String remark;


}
