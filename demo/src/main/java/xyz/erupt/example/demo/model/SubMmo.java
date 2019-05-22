package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.eruptlimit.model.BaseModel;
import xyz.erupt.core.handler.SimpleConditionHandler;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
        name = "subTest",
        loginUse = false,
        sorts = "id"
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
                    inputType = @InputType,
                    search = @Search(true)
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
                    search = @Search(true)
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
                                            @VL(value = "123", label = "张三"),
                                            @VL(value = "234", label = "李四")
                                    },
                                    type = ChoiceEnum.SELECT_SINGLE
                            )
                    },
                    search = @Search(true)
            )
    )
    private Integer choice;

    @EruptField(
            views = {
                    @View(title = "input", column = "age"),
                    @View(title = "年龄", column = "choice"),
            },
            edit = @Edit(
                    title = "mmo_table",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(id = "id", label = "name",
                            filter = @Filter(condition = "'id=@abc@'", conditionHandlers = {SimpleConditionHandler.class}))
            )
    )
    @ManyToOne
    @JoinColumn(name = "MMO_ID")
    private Mmo mmo;

    @Temporal(TemporalType.DATE)
    private Date date;
}
