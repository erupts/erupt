package xyz.erupt.designer.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.filter.EruptMenuViewFilter;

/**
 * @author YuePeng
 */
@Erupt(name = "Add to Menu")
@Getter
@Setter
public class DesignerReleaseModal extends BaseModel {

    @EruptField(
            edit = @Edit(title = "Menu Name", notNull = true)
    )
    private String name;

    @EruptField(
            edit = @Edit(
                    title = "Menu Location",
                    desc = "Skip if publishing to root directory",
                    search = @Search,
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id"),
                    filter = @Filter(conditionHandler = EruptMenuViewFilter.class)
            )
    )
    private EruptMenu eruptMenu;

}
