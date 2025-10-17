package xyz.erupt.sample.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Date;

@Erupt(name = "DEMO", dataProxy = DemoDataProxy.class, power = @Power(export = true, importable = true))
@Table(name = "t_demo")
@Entity
public class Demo extends BaseModel {

    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本", search = @Search(vague = true))
    )
    private String input;

    @EruptField(
            views = @View(title = "数值", sortable = true),
            edit = @Edit(title = "数值", search = @Search)
    )
    private Integer number = 100;  // Default value 100

    @EruptField(
            views = @View(title = "浮点", sortable = true),
            edit = @Edit(title = "浮点", search = @Search)
    )
    private Double dou = 100.1111D;  // Default value 100

    @EruptField(
            views = @View(title = "布尔"),
            edit = @Edit(title = "布尔", search = @Search)
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "时间"),
            edit = @Edit(title = "时间", search = @Search)
    )
    private Date date;

}
