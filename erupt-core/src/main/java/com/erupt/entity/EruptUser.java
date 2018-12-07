package com.erupt.entity;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.BoolType;
import com.erupt.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 11/22/18.
 */
@Erupt(
        name = "用户",
        desc = "用户配置"
)
@Entity
@Table(name = "E_USER")
public class EruptUser extends BaseModel {

    @EruptField(
            views = @View(title = "用户名"),
            edit = @Edit(title = "用户名", desc = "登录用户名", notNull = true))
    private String accout;


    @EruptField(
            views = @View(title = "姓名"),
            edit = @Edit(title = "姓名", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "密码"),
            edit = @Edit(title = "密码", notNull = true)
    )
    private String password;

    @EruptField(
            views = @View(title = "确认密码"),
            edit = @Edit(title = "确认密码", notNull = true)
    )
    private String rePassword;

    @EruptField(
            views = @View(title = "md5加密"),
            edit = @Edit(title = "md5加密",
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密",
                            defaultValue = false
                    )
            )
    )
    private Boolean isMD5;

    @EruptField(
            edit = @Edit(title = "所属角色")
    )
    private Role role;

    @Lob
    @EruptField(
            edit = @Edit(title = "ip白名单", desc = "ip与ip之间使用换行符间隔")
    )
    private String whiteIp;

}
