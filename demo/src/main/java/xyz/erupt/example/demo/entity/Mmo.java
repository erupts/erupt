package xyz.erupt.example.demo.entity;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.RgbColor;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.eruptlimit.model.BaseModel;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
//        filter = @Filter(condition = "'id=1'"),
        name = "测试",
        rowOperation = {@RowOperation(
                code = "action",
                icon = "fa fa-terminal",
                title = "执行",
                operationHandler = OperationHandlerImpl.class,
                edits = {
                        @CodeAndEdit(
                                code = "name",
                                codeType = "int",
                                edit = @Edit(title = "姓名", notNull = true)
                        ),
                        @CodeAndEdit(
                                code = "idCard",
                                codeType = "Integer",
                                edit = @Edit(title = "身份证号", notNull = true)
                        )
                }
        ), @RowOperation(code = "single", icon = "pic-left",
                title = "单个执行", multi = false,
                operationHandler = OperationHandlerImpl.class
//                edits = {
//                        @CodeAndEdit(code = "idcard", edit = @Edit(title = "身份证号")),
//                        @CodeAndEdit(code = "name", edit = @Edit(title = "姓名")),
//                }
        )
        },
        cards = {
                @Card(icon = "fa fa-table", value = "100", desc = "第一个卡片", color = RgbColor.green),
                @Card(icon = "fa fa-table", value = "200", desc = "第二个卡片", color = RgbColor.red),
                @Card(icon = "fa fa-table", value = "300", desc = "第三个卡片", color = RgbColor.blue),
                @Card(icon = "fa fa-table", value = "400", desc = "第四个卡片", color = RgbColor.yellow)
        },
        tree = @Tree(id = "id", label = "name", pid = "parent.id")
)
@Entity
@Table(name = "ERUPT_TEST")
public class Mmo extends BaseModel {

    @Column(name = "NAME")
    @EruptField(
            views = {
                    @View(title = "名称", className = "volcano", template = "名称：@txt@", sortable = true)
            },
            edit = @Edit(
                    desc = "名称",
                    title = "名称",
                    notNull = true,
                    inputType = @InputType(length = 100),
                    search = @Search(search = true)
            )
    )
    private String name;

    @Transient
    @EruptField(
            views = {
                    @View(title = "数字滑块")
            },
            edit = @Edit(
                    title = "数字滑块",
                    notNull = true,
                    type = EditType.SLIDER,
                    sliderType = @SliderType(max = 1000, min = -1000),
                    search = @Search(search = true, vague = true)
            )
    )
    private Number slider;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PARENT_ID")
    @EruptField(
            views = {
                    @View(title = "pid", column = "id")
            },
            edit = @Edit(
                    desc = "上级菜单",
                    title = "上级菜单",
                    notNull = true,
                    search = @Search(search = true),
                    type = EditType.REFERENCE,
                    referenceType = @ReferenceType(id = "id", label = "name", pid = "id", depend = "name")
            )
    )
    private Mmo parent;

    @EruptField(
            edit = @Edit(
                    title = "子节点",
                    type = EditType.TAB,
                    tabType = @TabType(type = TabEnum.TREE_SELECT)
            )
    )
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Mmo> children;


    @EruptField(
            views = {
                    @View(title = "number")
            },
            edit = @Edit(
                    title = "数字",
                    notNull = true,
                    search = @Search(search = true)
            )
    )
    @Column(name = "AGE")
    private Integer age;

    @Transient
    @EruptField(
            edit = @Edit(
                    title = "",
                    type = EditType.EMPTY
            )
    )
    private String empty;

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
                    search = @Search(search = true)
            )
    )
    private Integer choice;


    @EruptField(
            views = @View(title = "多选", viewType = ViewType.QR_CODE),
            edit = @Edit(
                    desc = "多个选择",
                    title = "多选",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceType = {
                            @ChoiceType(
                                    vl = {
                                            @VL(value = 1, label = "张三"),
                                            @VL(value = 2, label = "李四"),
                                            @VL(value = 3, label = "王五"),
                                            @VL(value = 4, label = "李四"),
                                            @VL(value = 5, label = "李四"),
                                            @VL(value = 6, label = "李四"),
                                            @VL(value = 7, label = "张三"),
                                            @VL(value = 8, label = "李四"),
                                            @VL(value = 888, label = "王五"),
                                            @VL(value = 999, label = "李四"),
                                            @VL(value = 991, label = "李四"),
                                            @VL(value = 992, label = "李四"),
                                    },
                                    type = ChoiceEnum.RADIO
                            )
                    },
                    search = @Search(search = true)
            )
    )
    private Integer single;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(
                    notNull = true,
                    title = "布尔",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "是的", falseText = "不行")
            )
    )
    private Boolean bool;

    @EruptField(
            views = @View(
                    title = "时间"
            ),
            edit = @Edit(
                    notNull = true,
                    title = "时间",
                    type = EditType.DATE,
                    dateType = @DateType(type = DateEnum.WEEK)
            )
    )
    private Date date;

    @EruptField(
            edit = @Edit(
                    title = "图片",
                    type = EditType.IMAGE
            )
    )
    private String image;


//    @EruptField(
//            edit = @Edit(
//                    title = "tab1",
//                    type = EditType.TAB
//            )
//    )
//    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "mmo")
//    private transient Set<SubMmo> sub;


    @Column(name = "TAG")
    @EruptField(
            views = {
                    @View(title = "标签", viewType = ViewType.LINK)
            },
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    inputType = @InputType(type = InputEnum.TEXTAREA)
            )
    )
    private String gender;

    @Column(name = "markDown")
    @EruptField(
            edit = @Edit(
                    title = "markDown",
                    notNull = true,
                    type = EditType.MARK_DOWN
            )
    )
    public String markDown;


    @Column(name = "KISS")
    @EruptField(
            edit = @Edit(
                    title = "ATTACHMENT",
                    notNull = true,
                    type = EditType.ATTACHMENT
            )
    )
    private String kiss;

    @Transient
    @EruptField(
            edit = @Edit(
                    title = "ATTACHMENT",
                    type = EditType.JSON_EDIT
            )
    )
    private String json;


    @Transient
    @EruptField(
            edit = @Edit(
                    title = "DependSwitchType",
                    type = EditType.DEPEND_SWITCH,
                    dependSwitchType = @DependSwitchType(
                            dependSwitchAttrs = {
                                    @DependSwitchAttr(name = "显示markdown和gender", val = 1, dependEdits = {"markDown", "gender"}),
                                    @DependSwitchAttr(name = "显示其他", val = 2, dependEdits = {"choice", "age", "parent", "name"})
                            }
                    )
            )
    )
    private Integer DependSwitchType;


}
