//package xyz.erupt.bi.view;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.SQLDelete;
//import org.hibernate.annotations.Where;
//import xyz.erupt.jpa.model.BaseModel;
//
//import jakarta.persistence.Lob;
//import jakarta.persistence.Table;
//import jakarta.persistence.UniqueConstraint;
//import java.util.Date;
//
///**
// * @author YuePeng
// * date 2022/7/19 00:17
// */
//@Getter
//@Setter
////@Entity
//@Table(name = "e_bi_view", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
//@Where(clause = "deleteTime is null")
//@SQLDelete(sql = "update e_bi_view set deleteTime = now() where id = ?")
//public class BiViewVo extends BaseModel {
//
//    //编码
//    private String code;
//
//    //名称
//    private String name;
//
//    //发布状态
//    private Boolean publish = false;
//
//    //编辑时配置
//    @Lob
//    private String editConfig;
//
//    //已发布配置
//    @Lob
//    private String viewConfig;
//
//    //删除时间
//    private Date deleteTime;
//
//}
