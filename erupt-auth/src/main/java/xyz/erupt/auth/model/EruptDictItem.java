package xyz.erupt.auth.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author liyuepeng
 * @date 2018-12-07.
 */

@Entity
@Table(name = "E_DICT_ITEM", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "字典项",
        orderBy = "sort"
)
public class EruptDictItem extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true, search = @Search(true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search(value = true, vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "排序", sortable = true),
            edit = @Edit(title = "排序")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;

    @ManyToOne
    private EruptDict eruptDict;


}
