package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.KV;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.eruptlimit.model.EruptRole;
import xyz.erupt.eruptlimit.model.EruptUser;

import javax.persistence.*;

/**
 * Created by liyuepeng on 2019-05-14.
 */
@Erupt(
        name = "测试",
        loginUse = false,
        primaryKeyCol = "testId",
        rowOperation = {
                @RowOperation(code = "a", icon = "fa fa-user", title = "AAA", operationHandler = OperationHandlerImpl.class)
        },
        params = {
                @KV(key = "label", value = "1+1", desc = "{balala 23333 +++ ,, []")
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
            views = @View(title = "姓名", sortable = true, template = "'姓名：'+item.name"),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(true))
    )
    private String name;

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
                    dateType = @DateType(type = DateEnum.DATE_TIME)
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
                    search = @Search(value = true),
                    type = EditType.REFERENCE_TREE,
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
                    type = EditType.REFERENCE_TREE,
                    placeHolder = "角色",
                    referenceTreeType = @ReferenceTreeType(depend = "eruptUser", dependColumn = "id"),
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
                    referenceTreeType = @ReferenceTreeType(depend = "name", dependColumn = "name"),
                    search = @Search(value = true)
            )
    )
    private TestExtra testExtra;

}
