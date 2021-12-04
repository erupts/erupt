//package xyz.erupt.bi.model;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.Type;
//import org.springframework.stereotype.Component;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.EruptI18n;
//import xyz.erupt.annotation.fun.OperationHandler;
//import xyz.erupt.annotation.sub_field.Edit;
//import xyz.erupt.annotation.sub_field.EditType;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.annotation.sub_field.ViewType;
//import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
//import xyz.erupt.annotation.sub_field.sub_edit.Search;
//import xyz.erupt.bi.constant.BiConst;
//import xyz.erupt.core.util.Erupts;
//import xyz.erupt.jpa.dao.EruptDao;
//import xyz.erupt.upms.enums.MenuStatus;
//import xyz.erupt.upms.helper.HyperModelUpdateVo;
//import xyz.erupt.upms.model.EruptMenu;
//import xyz.erupt.upms.service.EruptContextService;
//import xyz.erupt.upms.service.EruptUserService;
//
//import javax.annotation.Resource;
//import javax.persistence.*;
//import javax.transaction.Transactional;
//import java.util.List;
//import java.util.Set;
//
///**
// * @author YuePeng
// * date 2019-08-26.
// */
//@Entity
//@Table(name = "e_bi_group", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
//@Erupt(name = "报表组别")
//@Getter
//@Setter
//@Component
//@EruptI18n
//public class BiGroup extends HyperModelUpdateVo {
//
//    @EruptField(
//            views = @View(title = "编码", sortable = true),
//            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true))
//    )
//    private String code;
//
//    @EruptField(
//            views = @View(title = "名称", sortable = true),
//            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
//    )
//    private String name;
//
//
//}
