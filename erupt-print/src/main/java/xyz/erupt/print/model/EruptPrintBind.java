package xyz.erupt.print.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.upms.helper.HyperModelUpdateVo;


@Getter
@Setter
@Entity
@Table(name = "e_print_bind")
@Erupt(name = "Print Bind Erupt")
public class EruptPrintBind extends HyperModelUpdateVo {

    @EruptField(
            views = @View(title = "Erupt 模型")
    )
    private String erupt;

    @EruptField(
            views = @View(title = "内容")
    )
    private String content;
}
