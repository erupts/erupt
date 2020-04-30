package xyz.erupt.example.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.expr.Expr;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.service.EruptUserService;
import xyz.erupt.core.model.BaseModel;
import xyz.erupt.example.handler.HtmlHandler;
import xyz.erupt.example.handler.OperationHandlerImpl;
import xyz.erupt.example.handler.PowerHandlerImpl;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2019-10-08.
 */
@Erupt(name = "demo", orderBy = "number desc",
        power = @Power(export = true, importable = true, powerHandler = PowerHandlerImpl.class), dataProxy = Demo.class,
        rowOperation = {
                @RowOperation(operationHandler = OperationHandlerImpl.class,
                        ifExpr = "item.number", tip = "执行操作一需要填写，名称与菜单信息",
                        eruptClass = DemoSub.class, title = "操作一", icon = "fa fa-eercast", code = "d1"),
                @RowOperation(operationHandler = OperationHandlerImpl.class,
                        title = "操作文本", icon = "fa fa-table", code = "d2")
        },
        layoutTree = "eruptMenu",
        tree = @Tree(label = "input"),
        filter = @Filter("2 = 2")
)
@Table(name = "DEMO")
@Entity
@Component
public class Demo extends BaseModel implements DataProxy<Demo>, ChoiceFetchHandler {


    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本", search = @Search(vague = true), notNull = true)
    )
    private String input = "默认文本";

    @EruptField(
            views = @View(title = "数字"),
            edit = @Edit(title = "数字", search = @Search(vague = true))
    )
    private Integer number;

    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间", type = EditType.DATE, search = @Search, dateType = @DateType(type = DateType.Type.TIME))
    )
    private String time1;

    @Lob
    @EruptField(
            views = @View(title = "代码编辑器", desc = "CSS代码编辑器"),
            edit = @Edit(title = "代码编辑器", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "css"))
    )
    private String code;

    @EruptField(
            views = @View(title = "时间日期"),
            edit = @Edit(title = "时间日期", search = @Search(vague = true), dateType = @DateType(limit = true))
    )
    private Date date;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔", search = @Search)
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "选择"),
            edit = @Edit(
                    title = "选择", type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            fetchHandler = Demo.class,
                            fetchHandlerParams = {"param"},
                            vl = {
                                    @VL(label = "xxxx", value = "xxxx")
                            }
                    ))
    )
    private String choice;

    @EruptField(
            views = @View(title = "多选"),
            edit = @Edit(
                    title = "选择", type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            type = ChoiceEnum.SELECT_MULTI,
                            fetchHandler = Demo.class,
                            vl = {
                                    @VL(label = "xxxx", value = "xxxx")
                            },
                            fetchHandlerParams = {"param"}
                    ))
    )
    private String choiceCheckbox;

    @ManyToOne
    @EruptField(
            views = @View(title = "多对一树", column = "name"),
            edit = @Edit(title = "多对一树", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id", rootTagExpr = @Expr("EruptMenu.code='manager'"))
            )
    )
    private EruptMenu eruptMenu;

    @ManyToOne
    @EruptField(
            views = @View(title = "多对一表格", column = "name"),
            edit = @Edit(title = "多对一表格", type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(dependField = "eruptMenu"),
                    search = @Search)
    )
    private EruptUser eruptUser;

    @EruptField(
            views = @View(title = "图片", type = ViewType.IMAGE),
            edit = @Edit(title = "图片", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE))
    )
    private String img;


    @EruptField(
            views = @View(title = "附件上传", type = ViewType.DOWNLOAD),
            edit = @Edit(title = "附件上传", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType, placeHolder = "xxxxx")
    )
    private String attachment;


    @EruptField(
            views = @View(title = "大文本域"),
            edit = @Edit(title = "大文本域", search = @Search(vague = true))
    )
    private String remark;

    @EruptField(
            edit = @Edit(title = "HTML", type = EditType.TPL,
                    tplType = @Tpl(path = "demo.html", tplHandler = HtmlHandler.class, params = {"123", "xxx"}))
    )
    private String tpl;

    @EruptField(
            views = @View(title = "地图"),
            edit = @Edit(title = "地图", type = EditType.MAP, mapType = @MapType())
    )
    private String map;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "demo_sub_id")
    @EruptField(
            edit = @Edit(
                    title = "一对多",
                    type = EditType.TAB_TABLE_ADD
            )
    )
    private Set<DemoSub> demoSubs;

    @Transient
    @Autowired
    private EruptUserService eruptUserService;


    @Override
    public Map<String, String> fetch(String[] params) {
        Map<String, String> map = new HashMap<>();
        map.put(params[0], params[0]);
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        return map;
    }


}
