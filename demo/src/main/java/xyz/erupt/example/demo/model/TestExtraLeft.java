package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.eruptlimit.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 2019-07-05.
 */
@Erupt(name = "TestExtraLeft")
@Entity
@Table(name = "TEST_EXTRA_LEFT")
public class TestExtraLeft extends BaseModel {

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "姓名", sortable = true, template = "'姓名：'+item.name", viewType = ViewType.HTML),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(true))
    )
    private String name;
}
