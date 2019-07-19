package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.auth.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liyuepeng on 2019-07-18.
 */
@Erupt(
        name = "测试"
)
@Entity
@Table(name = "TEST_MANY")
public class TestMany extends BaseModel {

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "名称", sortable = true, viewType = ViewType.HTML),
            edit = @Edit(title = "名称", notNull = true, search = @Search(true))
    )
    private String name;
}
