package xyz.erupt.example.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-11-18.
 */
@Erupt(name = "车辆管理")
@Table(name = "CARD_MANAGER")
@Entity
public class CarManager extends BaseModel {

    @EruptField(
            views = @View(title = "车辆名称"),
            edit = @Edit(title = "车辆名称", search = @Search(value = true, vague = true), notNull = true)
    )
    private String input;

    @EruptField(
            views = @View(title = "采购日期"),
            edit = @Edit(title = "采购日期", search = @Search(value = true, vague = true), notNull = true)
    )
    private Date date;

    @EruptField(
            views = @View(title = "车辆状态"),
            edit = @Edit(title = "车辆状态", boolType = @BoolType(trueText = "可用", falseText = "不可用", defaultValue = true), search = @Search(value = true, vague = true), notNull = true)
    )
    private Boolean status;

    @EruptField(
            views = @View(title = "车辆图片", type = ViewType.IMAGE),
            edit = @Edit(title = "车辆图片", notNull = true)
    )
    private String image;

}
