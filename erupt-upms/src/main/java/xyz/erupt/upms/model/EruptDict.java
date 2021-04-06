package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.model.base.HyperModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author YuePeng
 * date 2018-12-07.
 */

@Entity
@Table(name = "e_dict", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "数据字典",
        power = @Power(export = true),
        drills = @Drill(
                title = "字典项",
                link = @Link(
                        linkErupt = EruptDictItem.class, joinColumn = "eruptDict.id"
                )
        )
)
@Getter
@Setter
public class EruptDict extends HyperModel {

    @EruptField(
            views = @View(title = "编码", sortable = true),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;

}
