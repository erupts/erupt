package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author YuePeng
 * date 2022/3/12 00:53
 */
@Entity
@Table(name = "e_bi_column")
@Erupt(name = "列配置")
@Getter
@Setter
@Component
@EruptI18n
public class BiColumn extends BaseModel {

    @EruptField(
            views = @View(title = "列名", sortable = true),
            edit = @Edit(title = "列名", notNull = true, desc = "设置列特殊属性时配置")
    )
    private String name;

    @EruptField(
            views = @View(title = "宽度", sortable = true),
            edit = @Edit(title = "宽度")
    )
    private Integer width;

    @EruptField(
            views = @View(title = "排序", sortable = true),
            edit = @Edit(title = "排序", notNull = true)
    )
    private Boolean orderBy = true;

    @EruptField(
            views = @View(title = "固定列", sortable = true),
            edit = @Edit(title = "固定列", notNull = true)
    )
    private Boolean fixed = false;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Bi bi;

}
