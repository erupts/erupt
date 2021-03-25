package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.model.base.HyperModel;

import javax.persistence.*;

/**
 * @author YuePeng
 * date 2018-12-07.
 */

@Entity
@Table(name = "e_dict_item", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "erupt_dict_id"}))
@Erupt(
        name = "字典项",
        orderBy = "sort",
        power = @Power(export = true)
)
@Getter
@Setter
public class EruptDictItem extends HyperModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true, search = @Search)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "显示顺序", sortable = true),
            edit = @Edit(title = "显示顺序")
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
    @EruptField
    @JoinColumn(name = "erupt_dict_id")
    private EruptDict eruptDict;

}
