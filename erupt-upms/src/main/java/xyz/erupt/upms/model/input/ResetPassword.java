package xyz.erupt.upms.model.input;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2022/12/10 14:29
 */
@Erupt(name = "重置密码")
@EruptI18n
@Getter
@Setter
public class ResetPassword extends BaseModel {

    @EruptField(
            edit = @Edit(title = "密码", notNull = true)
    )
    private String password;

    @EruptField(
            edit = @Edit(title = "确认密码", notNull = true)
    )
    private String password2;

    @EruptField(
            edit = @Edit(
                    title = "md5加密", type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密"
                    )
            )
    )
    private Boolean isMd5 = true;

}
