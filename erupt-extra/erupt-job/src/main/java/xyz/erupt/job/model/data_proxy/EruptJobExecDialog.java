package xyz.erupt.job.model.data_proxy;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2024/6/11 20:57
 */
@Erupt(name = "job execute")
@Getter
@Setter
@EruptI18n
public class EruptJobExecDialog extends BaseModel {

    @EruptField(
            edit = @Edit(title = "任务参数", type = EditType.CODE_EDITOR)
    )
    private String param;

}
