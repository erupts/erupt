package xyz.erupt.job.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

import javax.persistence.Lob;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
//@Erupt(
//        name = "任务处理日志"
//)
public class EruptJobLog {

    @EruptField(
            views = @View(title = "任务名称"),
            edit = @Edit(title = "任务名称", notNull = true, search = @Search(vague = true, value = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "任务参数"),
            edit = @Edit(title = "任务参数")
    )
    private String handlerParam;

    private Date startTime;

    private Date endTime;

    @Lob
    @EruptField(
            views = @View(title = "错误信息"),
            edit = @Edit(title = "错误信息")
    )
    private String errorInfo;

}
