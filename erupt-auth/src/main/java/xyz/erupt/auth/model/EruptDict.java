package xyz.erupt.auth.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Drill;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author liyuepeng
 * @date 2018-12-07.
 */

@Entity
@Table(name = "E_DICT", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "数据字典"
)
public class EruptDict extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", drill = @Drill(eruptClass = EruptDictItem.class, joinColumn = "")),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;


}
