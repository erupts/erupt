//package xyz.erupt.print.model;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.print.pojo.PrintPageConfig;
//import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;
//import xyz.erupt.upms.helper.HyperModelUpdateVo;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "e_print_config")
//@Erupt(name = "Print Config")
//public class EruptPrintConfig extends HyperModelUpdateVo {
//
//    @EruptField(
//            views = @View(title = "Erupt 模型")
//    )
//    private String erupt;
//
//    @EruptField(
//            views = @View(title = "内容")
//    )
//    private String content;
//
//    private PrintPageConfig pageConfig;
//
//}
