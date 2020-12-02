//package xyz.erupt.job.model;
//
//import lombok.Getter;
//import org.springframework.stereotype.Component;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.sub_field.Edit;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.annotation.sub_field.sub_edit.Search;
//import xyz.erupt.auth.model.base.HyperModel;
//
//import javax.persistence.Entity;
//import javax.persistence.Lob;
//import javax.persistence.Table;
//import javax.persistence.UniqueConstraint;
//
///**
// * @author liyuepeng
// * @date 2019-12-26
// */
//@Getter
//@Erupt(
//        name = "任务处理类"
//)
//@Entity
//@Table(name = "e_job_handler", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
//@Component
//public class EruptJobHandler extends HyperModel {
//
//    @EruptField(
//            views = @View(title = "任务编码"),
//            edit = @Edit(title = "任务编码", notNull = true, search = @Search)
//    )
//    private String code;
//
//    @EruptField(
//            views = @View(title = "名称"),
//            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
//    )
//    private String name;
//
//    @EruptField(
//            views = @View(title = "JOB处理类"),
//            edit = @Edit(title = "JOB处理类", desc = "需实现EruptJobHandler接口"
//                    , notNull = true, search = @Search(vague = true))
//    )
//    private String handler;
//
//    @Lob
//    @EruptField(
//            views = @View(title = "描述"),
//            edit = @Edit(title = "描述")
//    )
//    private String remark;
//}
