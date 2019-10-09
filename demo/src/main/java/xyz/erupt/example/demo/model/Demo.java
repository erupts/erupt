package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.auth.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 2019-10-08.
 */
@Erupt(name = "demo",orderBy = "number desc")
@Table(name = "DEMO")
@Entity
public class Demo extends BaseModel {

    @EruptField(
            views = @View(title = "文本输入"),
            edit = @Edit(title = "文本输入", search = @Search(value = true, vague = true))
    )
    private String input;

    @EruptField(
            views = @View(title = "数字输入"),
            edit = @Edit(title = "数字输入", search = @Search(value = true))
    )
    private Integer number;

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔", search = @Search(value = true, vague = true))
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "图片"),
            edit = @Edit(title = "图片", search = @Search(value = true, vague = true))
    )
    private String image;

    @EruptField(
            views = @View(title = "自定义HTML"),
            edit = @Edit(title = "HTML", type = EditType.HTML, search = @Search(value = true, vague = true))
    )
    private String html = "<h1 style='text-align:center'>lalalal</h1>";

    @EruptField(
            views = @View(title = "大文本域"),
            edit = @Edit(title = "大文本域", search = @Search(value = true, vague = true))
    )
    private String remark;
}
