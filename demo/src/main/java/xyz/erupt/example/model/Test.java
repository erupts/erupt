package xyz.erupt.example.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-02-29
 */
@Erupt(name = "demo", orderBy = "number desc", filter = @Filter(condition = "number = 11"))
@Table(name = "TEST")
@Entity
public class Test extends BaseModel {

    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本", search = @Search(value = true, vague = true))
    )
    private String input = "默认文本";

    @EruptField(
            views = @View(title = "数字"),
            edit = @Edit(title = "数字", search = @Search(value = true, vague = true))
    )
    private Integer number;
}
