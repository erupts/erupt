package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;

import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-03-30
 */
//@Entity
@Table(name = "E_BI_REFER_DIMENSION")
//@Erupt(name = "参照维度")
@Getter
@Setter
public class BiReferDimension {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称")
    )
    private String title;

    @EruptField(
            edit = @Edit(title = "参照SQL", type = EditType.TEXTAREA)
    )
    private String refSql;
}
