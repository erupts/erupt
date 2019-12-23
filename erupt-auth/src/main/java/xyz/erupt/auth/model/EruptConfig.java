package xyz.erupt.auth.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by liyuepeng on 12/7/18.
 */

@Entity
@Table(name = "E_CONFIG", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(
        name = "全局配置",
        orderBy = "EruptDict.sort"
)
public class EruptConfig extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

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
            views = @View(title = "排序"),
            edit = @Edit(title = "排序")
    )
    private Integer sort;

    @EruptField(
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;


}
