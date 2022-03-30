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

import javax.persistence.Entity;
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
            edit = @Edit(title = "列名", notNull = true, desc = "列为动态渲染，不做列相关配置时可忽略该选项")
    )
    private String name;

    @EruptField(
            views = @View(title = "宽度", sortable = true),
            edit = @Edit(title = "宽度")
    )
    private Integer width;

    @EruptField(
            views = @View(title = "是否显示", sortable = true),
            edit = @Edit(title = "是否显示", notNull = true)
    )
    private Boolean show;

    @EruptField(
            views = @View(title = "排序", sortable = true),
            edit = @Edit(title = "排序", notNull = true)
    )
    private Boolean sortable = true;

//    @EruptField(
//            views = @View(title = "下钻", sortable = true),
//            edit = @Edit(title = "下钻", notNull = true, type = EditType.CODE_EDITOR,
//                    codeEditType = @CodeEditorType(language = "sql"))
//    )
//    private String drill;

//    @EruptField(
//            views = @View(title = "固定列", sortable = true),
//            edit = @Edit(title = "固定列", notNull = true)
//    )
//    private Boolean fixed = false;

}
