package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.eruptlimit.model.BaseModel;
import xyz.erupt.eruptlimit.model.EruptRole;
import xyz.erupt.eruptlimit.model.EruptUser;

import javax.persistence.*;

/**
 * Created by liyuepeng on 2019-05-14.
 */
@Erupt(
        name = "测试",
        loginUse = false,
        params = {"66666666","23333333"}
)
@Entity
@Table(name = "TEST")
public class Test extends BaseModel {

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "姓名", sortable = true),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(true))
    )
    private String name;

    @Column(name = "AGE")
    @EruptField(
            views = @View(title = "年龄"),
            edit = @Edit(
                    title = "年龄",
                    search = @Search(value = true, vague = true)
            )
    )
    private Integer age = 18;

    @Column(name = "IS_18")
    @EruptField(
            views = @View(title = "是否18岁"),
            edit = @Edit(
                    title = "是否18岁",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(
                            trueText = "是的",
                            falseText = "还没有。。。。",
                            defaultValue = true
                    ),
                    search = @Search(true)
            )
    )
    private Boolean is18;


    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @EruptField(
            views = {
                    @View(title = "用户名", column = "name"),
                    @View(title = "账户", column = "account")
            },
            edit = @Edit(
                    title = "用户",
                    search = @Search(value = true),
                    type = EditType.REFERENCE,
                    referenceTreeType = @ReferenceTreeType
            )
    )
    private EruptUser eruptUser;


    @ManyToOne
    @JoinColumn(name = "ROLE_ID")
    @EruptField(
            views = @View(title = "角色", column = "name"),
            edit = @Edit(
                    title = "角色",
                    type = EditType.REFERENCE,
                    referenceTreeType = @ReferenceTreeType(depend = "name", dependColumn = "name"),
                    search = @Search(value = true)
            )
    )
    private EruptRole eruptRole;

}
