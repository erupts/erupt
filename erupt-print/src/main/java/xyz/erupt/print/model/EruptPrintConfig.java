//package xyz.erupt.print.model;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Convert;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.constant.AnnotationConst;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.print.pojo.PrintPageConfig;
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
//            views = @View(title = "Erupt Model")
//    )
//    private String erupt;
//
//    @EruptField(
//            views = @View(title = "Content")
//    )
//    private String content;
//
//    @Column(length = AnnotationConst.CONFIG_LENGTH)
//    @Convert(converter = PrintPageConfig.class)
//    private PrintPageConfig pageConfig;
//
//}
