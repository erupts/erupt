package xyz.erupt.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "InputEdit")
public class InputModel extends BaseModel {

    // 必填 + 占位符
    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, placeHolder = "Enter name",
                    type = EditType.INPUT)
    )
    private String name;

    // 长度限制 + 铺满整行
    @Column(length = 500)
    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description", desc = "Max 500 chars",
                    type = EditType.INPUT,
                    inputType = @InputType(length = 500, fullSpan = true))
    )
    private String description;

    // 邮箱类型
    @EruptField(
            views = @View(title = "Email"),
            edit = @Edit(title = "Email", type = EditType.INPUT,
                    inputType = @InputType(type = "email"))
    )
    private String email;

    // 正则校验（手机号）
    @EruptField(
            views = @View(title = "Phone"),
            edit = @Edit(title = "Phone", type = EditType.INPUT,
                    inputType = @InputType(regex = "^1[3-9]\\d{9}$"))
    )
    private String phone;

    // 关闭自动去空格
    @EruptField(
            views = @View(title = "Code"),
            edit = @Edit(title = "Code", desc = "Whitespace is preserved",
                    type = EditType.INPUT,
                    inputType = @InputType(autoTrim = false))
    )
    private String code;

    // 前缀 + 后缀
    @EruptField(
            views = @View(title = "Price"),
            edit = @Edit(title = "Price", type = EditType.INPUT,
                    inputType = @InputType(
                            prefix = {@VL(value = "¥", label = "CNY")},
                            suffix = {@VL(value = "元", label = "Yuan")}))
    )
    private String price;

    // 启用模糊搜索
    @EruptField(
            views = @View(title = "Keyword"),
            edit = @Edit(title = "Keyword", type = EditType.INPUT,
                    search = @Search(value = true, vague = true))
    )
    private String keyword;

    // 编辑时只读
    @EruptField(
            views = @View(title = "Serial No"),
            edit = @Edit(title = "Serial No",
                    readonly = @Readonly(add = false, edit = true),
                    type = EditType.INPUT)
    )
    private String serialNo;

    // 不在表单中展示（仅列表可见）
    @EruptField(
            views = @View(title = "Internal Code"),
            edit = @Edit(title = "Internal Code", show = false, type = EditType.INPUT)
    )
    private String internalCode;
}
