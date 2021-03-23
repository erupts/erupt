package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_post", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Erupt(name = "用户岗位", orderBy = "weight desc")
@Getter
@Setter
public class EruptPost extends BaseModel {

    @EruptField(
            views = @View(title = "岗位编码"),
            edit = @Edit(title = "岗位编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "岗位名称"),
            edit = @Edit(title = "岗位名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "岗位权重"),
            edit = @Edit(title = "岗位权重", desc = "数值越高，岗位级别越高", notNull = true)
    )
    private Integer weight;

}
