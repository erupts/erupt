package xyz.erupt.tool.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author liyuepeng
 * @date 12/7/18.
 */

@Entity
@Table(name = "E_SYSTEM_SERVER", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "参数设置",
        orderBy = "sort"
)
public class EruptConfig extends BaseModel {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "键"),
            edit = @Edit(title = "键", notNull = true)
    )
    private String key;

    @EruptField(
            views = @View(title = "值"),
            edit = @Edit(title = "值", notNull = true)
    )
    private String value;

    @EruptField(
            views = @View(title = "缓存时间(秒)"),
            edit = @Edit(title = "缓存时间(秒)")
    )
    private Integer cacheTime;

    @EruptField(
            views = @View(title = "显示顺序"),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @Lob
    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;


}
