package xyz.erupt.job.model;

import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
@Erupt(
        name = "job任务处理"
)
@Entity
@Table(name = "E_JOB", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
public class EruptJobModel extends BaseModel implements DataProxy<EruptJobModel> {

    @EruptField(
            views = @View(title = "任务编码"),
            edit = @Edit(title = "任务编码", notNull = true, search = @Search(true))
    )
    private String code;

    @EruptField(
            views = @View(title = "任务名称"),
            edit = @Edit(title = "任务名称", notNull = true, search = @Search(vague = true, value = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "Cron表达式"),
            edit = @Edit(title = "Cron表达式", notNull = true)
    )
    private String cron;

    @EruptField(
            views = @View(title = "JOB处理类"),
            edit = @Edit(title = "JOB处理类", notNull = true, search = @Search(vague = true, value = true))
    )
    private String handler;

    @Lob
    @EruptField(
            views = @View(title = "任务参数"),
            edit = @Edit(title = "任务参数", type = EditType.TEXTAREA)
    )
    private String handlerParam;

    @EruptField(
            views = @View(title = "任务状态"),
            edit = @Edit(title = "任务状态", boolType = @BoolType(
                    trueText = "启用", falseText = "禁用"
            ), notNull = true, search = @Search(true))
    )
    private boolean status;

    @Lob
    @EruptField(
            views = @View(title = "描述"),
            edit = @Edit(title = "描述")
    )
    private String remark;


    @Override
    public void beforeUpdate(EruptJobModel eruptJobModel) {

    }

    @Override
    public void beforeAdd(EruptJobModel eruptJobModel) {

    }
}
