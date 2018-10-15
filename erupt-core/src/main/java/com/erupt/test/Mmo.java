package com.erupt.test;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_erupt.Power;
import com.erupt.annotation.sub_erupt.RowOperation;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.Search;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.ChoiceEnum;
import com.erupt.annotation.sub_field.sub_edit.ChoiceType;
import com.erupt.annotation.sub_field.sub_edit.VL;
import com.erupt.model.BaseModel;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
        name = "测试",
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
public class Mmo extends BaseModel {


    @Column(name = "NAME")
    @EruptField(
            views = {
                    @View(title = "张三", column = "abc"),
                    @View(title = "张三", column = "def"),
                    @View(title = "张三", column = "kkk")
            },
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    type = EditType.INPUT
            ),
            search = @Search(isSearch = true)
    )
    private String name;

    @Column(name = "TEXT")
    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(value = 123, label = "张三"),
                                    @VL(value = 123, label = "李四"),
                                    @VL(value = 123, label = "王五")
                            },
                            choiceEnum = ChoiceEnum.SING
                    )
            ),
            search = @Search(isSearch = true)
    )
    private String text;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
