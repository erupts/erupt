package xyz.erupt.print.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.MetaModelCreateOnlyVo;
import xyz.erupt.upms.helper.HyperModelUpdateVo;

@Getter
@Setter
@Entity
@Table(name = "e_print_tpl")
@Erupt(name = "Print Template")
public class EruptPrintTpl extends HyperModelUpdateVo {

    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称")
    )
    private String name;

    @EruptField(
            views = @View(title = "内容"),
            edit = @Edit(title = "内容")
    )
    private String content;

}
