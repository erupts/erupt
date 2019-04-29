package xyz.erupt.eruptlimit.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputEnum;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;

import javax.persistence.*;

/**
 * Created by liyuepeng on 12/7/18.
 */

@Entity
@Table(name = "E_DICT")
@Erupt(name = "数据字典", sorts = "sort")
public class EruptDict extends BaseModel {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    @Column(name = "CODE")
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    @Column(name = "NAME")
    private String name;


    @EruptField(
            views = @View(title = "上级菜单", column = "name"),
            edit = @Edit(
                    title = "上级菜单",
                    type = EditType.REFERENCE,
                    referenceTreeType = @ReferenceTreeType
            )
    )
    @ManyToOne
    @JoinColumn(name = "PARENT_DICT_ID")
    private EruptDict parent;

    @EruptField(
            views = @View(title = "排序"),
            edit = @Edit(title = "排序")
    )
    @Column(name = "SORT")
    private Integer sort;

    @EruptField(
            edit = @Edit(
                    title = "备注",
                    type = EditType.TEXTAREA
            )
    )
    @Column(name = "REMARK")
    private String remark;


}
