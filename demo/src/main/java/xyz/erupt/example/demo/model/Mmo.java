package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.CodeAndEdit;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.AttachmentEnum;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.ImageType;
import xyz.erupt.eruptlimit.model.BaseModel;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt(
//        filter = @Filter(condition = "'id=1'"),
        name = "测试",
        loginUse = false,
        rowOperation = {@RowOperation(
                code = "action",
                icon = "fa-terminal",
                title = "执行",
                operationHandler = OperationHandlerImpl.class,
                edits = {
                        @CodeAndEdit(
                                code = "name",
                                codeType = "Integer",
                                edit = @Edit(title = "姓名", notNull = true)
                        ),
                        @CodeAndEdit(
                                code = "idCard",
                                codeType = "Integer",
                                edit = @Edit(title = "身份证号", notNull = true)
                        )
                }
        ), @RowOperation(code = "single", icon = "fa-star",
                title = "单个执行", multi = false,
                operationHandler = OperationHandlerImpl.class,
                edits = {
                        @CodeAndEdit(code = "idCard", edit = @Edit(title = "身份证号")),
                        @CodeAndEdit(code = "name", edit = @Edit(title = "姓名")),
                }
        )
        },
        tree = @Tree(id = "id", label = "name", pid = "parent.id")
)
@Entity
@Table(name = "ERUPT_TEST")
public class Mmo extends BaseModel {

    @Transient
    @EruptField(
            edit = @Edit(
                    title = "DependSwitchType",
                    type = EditType.DEPEND_SWITCH,
                    dependSwitchType = @DependSwitchType(
                            dependSwitchAttrs = {
                                    @DependSwitchAttr(label = "显示markdown和gender", value = 1, dependEdits = {"markDown", "gender"}),
                                    @DependSwitchAttr(label = "显示其他", value = 2, dependEdits = {"choice", "age", "parent", "name", "slider", "divide"})
                            }
                    )
            )
    )
    private Integer DependSwitchType;

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
    private Integer slider;


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
                    referenceType = @ReferenceType(id = "id", label = "name", pid = "id", depends = "name")
            )
    )
    private Mmo parent;

    @EruptField(
            edit = @Edit(
                    title = "子节点",
                    type = EditType.TAB,
                    tabType = @TabType(type = TabEnum.TABLE)
            )
    )
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Mmo> children;


    @EruptField(
            views = {
                    @View(title = "数字",viewType = ViewType.QR_CODE)
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
                    title = "华丽的分割线",
                    type = EditType.DIVIDE
            )
    )
    private String divide;

    @Transient
    @EruptField(
            edit = @Edit(
                    title = "",
                    type = EditType.HIDDEN
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
                                            @VL(value = "123", label = "张三"),
                                            @VL(value = "234", label = "李四"),
                                            @VL(value = "456", label = "王五")
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
                                            @VL(value = "1", label = "张三"),
                                            @VL(value = "2", label = "李四"),
                                            @VL(value = "3", label = "王五"),
                                            @VL(value = "4", label = "李四"),
                                            @VL(value = "5", label = "李四"),
                                            @VL(value = "6", label = "李四"),
                                            @VL(value = "7", label = "张三"),
                                            @VL(value = "8", label = "李四"),
                                            @VL(value = "888", label = "王五"),
                                            @VL(value = "999", label = "李四"),
                                            @VL(value = "991", label = "李四"),
                                            @VL(value = "992", label = "李四"),
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
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentEnum.IMAGE,
                            path = "abc",
                            fileTypes = {"png", "jpg"},
                            imageType = @ImageType(
                                    width = {1000, 2000},
                                    height = {0, 2000}
                            )
                    )
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
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType

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

}
