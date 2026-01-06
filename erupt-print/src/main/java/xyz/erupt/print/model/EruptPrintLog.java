package xyz.erupt.print.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.MetaModelCreateOnlyVo;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

@Getter
@Setter
@Entity
@Table(name = "e_print_log")
@Erupt(name = "Print Log")
public class EruptPrintLog extends HyperModelCreatorOnlyVo {

    @EruptField(
            views = @View(title = "打印标识")
    )
    private String seq;

    @EruptField(
            views = @View(title = "Erupt 模型")
    )
    private String erupt;

    @EruptField(
            views = @View(title = "内容")
    )
    private String content;

}
