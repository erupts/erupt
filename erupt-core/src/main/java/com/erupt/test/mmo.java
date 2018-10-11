package com.erupt.test;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_erupt.Power;
import com.erupt.annotation.sub_erupt.RowOperation;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.Search;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.ChoiceType;
import com.erupt.annotation.sub_field.sub_edit.VL;

import javax.persistence.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
        name = "测试",
        power = @Power(add = false),
        rowOperation = @RowOperation(
                icon = "fa fa-icon",
                title = "执行",
                operationHandler = OperationHandlerImpl.class,
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
        )
)
@Entity
@Table(name = "ERUPT_TEST")
public class mmo {
    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "NAME")
    @EruptField(
            multiView = {
                    @View(title = "张三", name = "abc")
            },
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    type = EditType.INPUT
            ),
            search = @Search(isSearch = true)
    )
    public String name;

    @Column(name = "TEXT")
    @EruptField(
            view = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(value = 123, label = "张三"),
                                    @VL(value = 123, label = "李四"),
                                    @VL(value = 123, label = "王五")
                            }
                    )
            ),
            search = @Search(isSearch = true)
    )
    public String text;

}
