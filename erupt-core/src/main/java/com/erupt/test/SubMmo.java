package com.erupt.test;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.constant.RgbColor;
import com.erupt.annotation.constant.UiColor;
import com.erupt.annotation.sub_erupt.Card;
import com.erupt.annotation.sub_erupt.RowOperation;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.sub_field.Search;
import com.erupt.annotation.sub_field.View;
import com.erupt.annotation.sub_field.sub_edit.*;
import com.erupt.model.BaseModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
        name = "sub test",
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
@Table(name = "ERUPT_SUB_TEST")
public class SubMmo extends BaseModel {


    @Column(name = "NAME")
    @EruptField(
            views = {
                    @View(title = "张三")
            },
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    inputType = @InputType(color = UiColor.success),
                    search = @Search(isSearch = true)
            )
    )
    private String name;


    @EruptField(
            views = {
                    @View(title = "number")
            },
            edit = @Edit(
                    title = "数字",
                    notNull = true,
                    search = @Search(isSearch = true)
            )
    )
    private Integer number;

    @Column(name = "TEXT")
    @EruptField(
            views = @View(title = "选择"),
            edit = @Edit(
                    title = "选择",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceType = {
                            @ChoiceType(
                                    vl = {
                                            @VL(value = 123, label = "张三"),
                                            @VL(value = 234, label = "李四"),
                                            @VL(value = 456, label = "王五")
                                    },
                                    type = ChoiceEnum.SELECT_SINGLE
                            )
                    },
                    search = @Search(isSearch = true)
            )
    )
    private String choice;

    @EruptField(
            views = {
                    @View(title = "input", column = "age"),
                    @View(title = "年龄", column = "text"),
            },
            edit = @Edit(
                    title = "tab1",
                    type = EditType.REFERENCE,
                    referenceType = @ReferenceType(id = "id", label = "name")
            )
    )
    @ManyToOne
    @JoinColumn(name = "MMO_ID")
    private Mmo mmo;

    @Temporal(TemporalType.DATE)
    private Date date;
}
