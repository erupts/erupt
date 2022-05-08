package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptMenu;

/**
 * @author YuePeng
 * date 2021/7/17 23:04
 */
@Erupt(name = "报表发布弹窗")
@Getter
@Setter
@EruptI18n
public class BiReleaseModal extends BaseModel {

    @EruptField(
            edit = @Edit(title = "菜单名称", notNull = true)
    )
    private String name;

    @EruptField(
            edit = @Edit(
                    search = @Search,
                    title = "菜单位置", desc = "发布至根目录可跳过此选项", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id")
            )
    )
    private EruptMenu eruptMenu;


}
