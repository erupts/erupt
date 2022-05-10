package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
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
            edit = @Edit(title = "列名", notNull = true, desc = "列为动态渲染，不做列相关配置请忽略列配置下各选项")
    )
    private String name;

    @EruptField(
            views = @View(title = "是否显示", sortable = true),
            edit = @Edit(title = "是否显示", notNull = true)
    )
    private Boolean display = true;

    @EruptField(
            views = @View(title = "是否排序", sortable = true),
            edit = @Edit(title = "是否排序", notNull = true)
    )
    private Boolean sortable = true;

    @EruptField(
            views = @View(title = "宽度", sortable = true),
            edit = @Edit(title = "宽度")
    )
    private Integer width;

    //下钻方式
//    @EruptField(
//            views = @View(title = "下钻", sortable = true),
//            edit = @Edit(title = "下钻", type = EditType.CHOICE, choiceType = @ChoiceType(
//                    vl = {
//                            @VL(value = "", label = "关闭"),
//                            @VL(value = "table", label = "表格")
//                    }
//            ))
//    )
//    private String drillType = "close";

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "下钻SQL"),
            edit = @Edit(title = "下钻SQL", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql"))
    )
    private String drillExpress;

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
