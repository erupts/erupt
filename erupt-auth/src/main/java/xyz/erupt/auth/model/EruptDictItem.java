//package xyz.erupt.auth.model;
//
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.sub_field.Edit;
//import xyz.erupt.annotation.sub_field.EditType;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
//
//import javax.persistence.*;
//
///**
// * @author liyuepeng
// * @date 12/7/18.
// */
//
//@Entity
//@Table(name = "E_DICT_ITEM", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
//@Erupt(
//        name = "数据字典项",
//        orderBy = "EruptDict.sort"
//)
//public class EruptDictItem extends BaseModel {
//
//    @EruptField(
//            views = @View(title = "编码"),
//            edit = @Edit(title = "编码", notNull = true)
//    )
//    @Column(name = "CODE")
//    private String code;
//
//    @EruptField(
//            views = @View(title = "名称"),
//            edit = @Edit(title = "名称", notNull = true)
//    )
//    @Column(name = "NAME")
//    private String name;
//
//
//    @EruptField(
//            views = @View(title = "上级菜单", column = "name"),
//            edit = @Edit(
//                    title = "上级菜单",
//                    type = EditType.REFERENCE_TREE,
//                    referenceTreeType = @ReferenceTreeType(pid = "id")
//            )
//    )
//    @ManyToOne
//    @JoinColumn(name = "PARENT_DICT_ID")
//    private EruptDictItem parent;
//
//    @EruptField(
//            views = @View(title = "排序"),
//            edit = @Edit(title = "排序")
//    )
//    @Column(name = "SORT")
//    private Integer sort;
//
//    @EruptField(
//            edit = @Edit(
//                    title = "备注"
//            )
//    )
//    @Column(name = "REMARK")
//    private String remark;
//
//
//}
