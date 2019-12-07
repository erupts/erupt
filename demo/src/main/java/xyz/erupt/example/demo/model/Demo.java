package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Html;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.auth.model.BaseModel;
import xyz.erupt.example.demo.handler.HtmlHandler;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by liyuepeng on 2019-10-08.
 */
@Erupt(name = "demo", orderBy = "number desc", power = @Power(export = true))
@Table(name = "DEMO")
@Entity
public class Demo extends BaseModel {

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
            views = @View(title = "时间")
//            edit = @Edit(title = "时间输入", search = @Search(value = true, vague = true))
    )
    private Date date;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔", search = @Search(true))
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "图片", type = ViewType.IMAGE),
            edit = @Edit(title = "图片")
    )
    private String image;


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

    @EruptField(
            views = @View(title = "自定义HTML"),
            edit = @Edit(title = "HTML", type = EditType.HTML,
                    htmlType = @Html(path = "demo.html", htmlHandler = HtmlHandler.class, params = {"123", "xxx"}),
                    search = @Search(true))
    )
    private String html;
}
