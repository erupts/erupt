package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.KV;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptRole;
import xyz.erupt.auth.model.EruptUser;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by liyuepeng on 2019-05-14.
 */
@Erupt(
        name = "测试",
        loginUse = false,
        primaryKeyCol = "testId",
        rowOperation = {
                @RowOperation(code = "a", icon = "fa fa-user", title = "AAA",
                        operationHandler = OperationHandlerImpl.class, eruptClass = TestExtra.class)
        },
        param = {
                @KV(key = "label", value = "1+1", desc = "{balala 223333 []")
        }
)
@Entity
@Table(name = "TEST")
//@EruptDataProcessor(processors = NonDataService.class)
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @EruptField
    private Long testId;

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "姓名", sortable = true, template = "'姓名：'+item.name", viewType = ViewType.HTML),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(true))
    )
    private String name = "2333333";

    @Column(name = "PRICE")
    @EruptField(
            views = @View(title = "价格", sortable = true),
            edit = @Edit(title = "价格", notNull = true, search = @Search(true))
    )
    private Double price;


    @Column(name = "RADIO")
    @EruptField(
            views = @View(
                    title = "单选"
            ),
            edit = @Edit(
                    title = "单选",
                    type = EditType.CHOICE,
                    search = @Search(value = true, vague = true),
                    choiceType = @ChoiceType(vl = {
                            @VL(label = "张A", value = "A"),
                            @VL(label = "张B", value = "B"),
                            @VL(label = "张C", value = "C"),
                            @VL(label = "张D", value = "D"),
                            @VL(label = "张E", value = "E"),
                            @VL(label = "张F", value = "F")
                    }, type = ChoiceEnum.RADIO)
            )
    )
    private String radio;


    @ManyToOne
    @JoinColumn(name = "MENU_ID")
    @EruptField(
            views = @View(title = "菜单", column = "name"),
            edit = @Edit(
                    title = "菜单",
                    type = EditType.REFERENCE_TREE,
                    placeHolder = "菜单",

                    referenceTreeType = @ReferenceTreeType(rootLabel = "系统管理", pid = "parentMenu.id"),
                    search = @Search(value = true)
            )
    )
    private EruptMenu eruptMenu;


    @Column(name = "CHOICE")
    @EruptField(
            views = @View(
                    title = "CHOICE View"
            ),
            edit = @Edit(
                    title = "CHOICE",
                    type = EditType.CHOICE,
                    search = @Search(value = true, vague = true),
                    choiceType = @ChoiceType(vl = {
                            @VL(label = "A", value = "张三"),
                            @VL(label = "李四", value = "李四")
                    }, type = ChoiceEnum.TAGS)
//                    inputType = @InputType(regex = "^1[3|4|5|7|8][0-9]\\d{8}$")
            )
    )
    private String choice;

    @Column(name = "CHECKBOX")
    @EruptField(
            edit = @Edit(
                    title = "手机号",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(label = "A", value = "张三"),
                            @VL(label = "李四", value = "李四")
                    }, type = ChoiceEnum.SELECT_MULTI)

            )
    )
    private String checkbox;


    @EruptField(
            edit = @Edit(
                    title = "手机号",
                    inputType = @InputType(regex = "^1[3|4|5|7|8][0-9]\\d{8}$")
            )
    )
    private String phone;


    @Column(name = "LINK")
    @EruptField(
            views = @View(title = "网址", viewType = ViewType.LINK_DIALOG),
            edit = @Edit(
                    title = "网址",
                    placeHolder = "网址",
                    search = @Search(value = true)
            )
    )
    private String link;


    @Column(name = "SWF_FILE")
    @EruptField(
            views = @View(title = "头像", viewType = ViewType.SWF, export = false),
            edit = @Edit(
                    title = "头像",
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentType.Type.OTHER,
                            maxLimit = 5,
                            fileTypes = "swf"
                    )
            )
    )
    private String swf;


    @Column(name = "SVG")
    @EruptField(
            views = @View(title = "SVG", viewType = ViewType.ATTACHMENT_DIALOG, export = false),
            edit = @Edit(
                    title = "SVG",
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentType.Type.OTHER,
                            maxLimit = 5,
                            fileTypes = "svg"
                    )
            )
    )
    private String svg;

    @Column(name = "PDF")
    @EruptField(
            views = @View(title = "PDF", viewType = ViewType.ATTACHMENT, export = false),
            edit = @Edit(
                    title = "PDF",
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentType.Type.OTHER,
                            maxLimit = 5
                    )
            )
    )
    private String pdf;

    @Column(name = "AGE")
    @EruptField(
            views = @View(title = "年龄"),
            edit = @Edit(
                    title = "年龄",
                    search = @Search(value = true, vague = true),
                    placeHolder = "请输入年龄"
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
                            trueText = "是",
                            falseText = "还没有。。。。",
                            defaultValue = true
                    ),
                    search = @Search(true)
            )
    )
    private Boolean is18;

    @Column(name = "DATE")
    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(
                    notNull = true,
                    title = "时间",
                    placeHolder = "时间",
                    search = @Search(value = true),
                    type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.DATE_TIME)
            )
    )
    private String date;

//    @Column(name = "MAP")
//    @EruptField(
//            views = @View(title = "地图", sortable = true),
//            edit = @Edit(title = "地图",
//                    type = EditType.MAP,
//                    notNull = true
//            )
//
//    )
//    private String map;


    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @EruptField(
            views = {
                    @View(title = "姓名", column = "name"),
                    @View(title = "用户名", column = "account")
            },
            edit = @Edit(
                    title = "用户",
                    type = EditType.REFERENCE_TABLE,
                    search = @Search(value = true)
            )
    )
    private EruptUser eruptUser;


    @ManyToOne
    @JoinColumn(name = "ROLE_ID")
    @EruptField(
            views = @View(title = "角色", column = "name"),
            edit = @Edit(
                    title = "角色",
                    type = EditType.REFERENCE_TREE,
                    placeHolder = "角色",
                    referenceTreeType = @ReferenceTreeType(dependField = "eruptUser", dependColumn = "id"),
                    search = @Search(value = true)
            )
    )
    private EruptRole eruptRole;

    @Transient
    @EruptField(
            edit = @Edit(
                    title = "其他信息",

                    type = EditType.DIVIDE,
                    readOnly = true,
                    search = @Search(value = true)
            )
    )
    private String aaa;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "BOOK_EXTRA_ID")
    @EruptField(
            views = {
                    @View(title = "是否加密", column = "isMD5"),
                    @View(title = "周", column = "week")
            },
            edit = @Edit(
                    title = "其他信息",
                    type = EditType.COMBINE,
                    referenceTreeType = @ReferenceTreeType(dependField = "name", dependColumn = "name"),
                    search = @Search(value = true)
            )
    )
    private TestExtra testExtra;

    @Lob
    @Column(name = "REMARK")
    @EruptField(
            views = @View(title = "描述", viewType = ViewType.HTML),
            edit = @Edit(
                    title = "描述",
                    type = EditType.HTML_EDIT
            )
    )
    private String remark;


    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL)
    @EruptField(
            edit = @Edit(
                    title = "MANY",
                    type = EditType.TAB_TABLE_ADD
            )
    )
    private Set<TestTestMany> testTestMany;
}
