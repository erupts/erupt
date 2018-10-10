package com.erupt.test;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_erupt.Power;
import com.erupt.annotation.sub_erupt.RowOperation;
import com.erupt.annotation.sub_field.*;
import com.erupt.annotation.sub_field.sub_edit.ChoiceType;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.sub_edit.InputEnum;
import com.erupt.annotation.sub_field.sub_edit.InputType;
import com.erupt.annotation.sub_field.sub_edit.ReferenceType;

import javax.persistence.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
        power = @Power(add = false),
        rowOperation = {@RowOperation(
                icon = "fa fa-icon",
                title = "执行",
                oerationHandler = OperationHandlerImpl.class,
                edits = {
                        @Edit(
                                title = "姓名",
                                notNull = true
                        ),
                        @Edit(
                                title = "身份证号",
                                notNull = true
                        )
                }
        )}
)
@Entity
@Table(name = "ERUPT_TEST")
public class mmo {


    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "NAME")
    @EruptField(
            view = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    type = EditType.INPUT,
                    inputType = @InputType(placeholder = "请输入姓名", inputEnum = InputEnum.NUMBER),
                    choiceType = @ChoiceType(values = {"张三", "李四"})),
            search = @Search(isSearch = true)
    )
    public String name;


    @EruptField(
            multiView = {
                    @View(column = "name", title = "用户名"),
                    @View(column = "password", title = "密码")
            },
            edit = @Edit(
                    title = "用户",
                    notNull = true,
                    type = EditType.REFERENCE,
                    referenceType = @ReferenceType(label = "name", id = "id")
            ),
            search = @Search(isSearch = true)
    )
    public User user;
}
