package xyz.erupt.bi.model;

import lombok.Getter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI_SCRIPT")
@Erupt(name = "报表函数")
@Getter
public class BiFunction extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true, value = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true, value = true))
    )
    private String name;

    @EruptField(
            edit = @Edit(title = "函数表达式", desc = "参照JavaScript function写法", notNull = true, search = @Search(vague = true, value = true))
    )
    private String function;

}
