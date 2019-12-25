package xyz.erupt.example.demo.model;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.service.EruptUserService;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by liyuepeng on 2019-10-08.
 */
@Erupt(name = "demo", orderBy = "number desc", power = @Power(export = true), dataProxy = Demo.class)
@Table(name = "DEMO")
@Entity
@Component
public class Demo extends BaseModel implements DataProxy<Demo>, ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本输入", search = @Search(value = true, vague = true))
    )
    private String input;

    @EruptField(
            views = @View(title = "数字"),
            edit = @Edit(title = "数字输入", search = @Search(value = true, vague = true))
    )
    private Integer number;

    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间", search = @Search(value = true, vague = true))
    )
    private Date date;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔", search = @Search(true))
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "选择"),
            edit = @Edit(title = "选择", type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = Demo.class,
                            vl = {
                                    @VL(label = "xxxx", value = "xxxx")
                            },
                            fetchHandlerParams = {"param"}))
    )
    private String choice;

    @ManyToOne
    @EruptField(
            views = @View(title = "多对一", column = "name"),
            edit = @Edit(title = "多对一", type = EditType.REFERENCE_TREE, search = @Search(value = true))
    )
    private EruptUser eruptUser;

    @EruptField(
            views = @View(title = "图片", type = ViewType.IMAGE),
            edit = @Edit(title = "图片", search = @Search(value = true, vague = true), type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE))
    )
    private String attachment;


    @EruptField(
            views = @View(title = "flash动画", type = ViewType.SWF),
            edit = @Edit(title = "swf", search = @Search(value = true, vague = true), type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(fileTypes = "swf"))
    )
    private String swf;


    @EruptField(
            views = @View(title = "大文本域"),
            edit = @Edit(title = "大文本域", search = @Search(value = true, vague = true))
    )
    private String remark;

//    @EruptField(
//            views = @View(title = "自定义HTML"),
//            edit = @Edit(title = "HTML", type = EditType.HTML,
//                    htmlType = @Html(path = "demo.html", htmlHandler = HtmlHandler.class, params = {"123", "xxx"}),
//                    search = @Search(true))
//    )
//    private String html;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "demo_id")
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
    public String beforeFetch(JsonObject condition) {
        System.out.println(eruptUserService.getCurrentUId());
        return null;
    }

    @Override
    public Map<String, String> fetch(String[] params) {
        System.out.println(params[0]);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        return map;
    }
}
