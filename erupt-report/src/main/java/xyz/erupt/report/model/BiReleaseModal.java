package xyz.erupt.report.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
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
 * date 2021/7/17 23:04
 */
@Erupt(name = "Publish Report")
@Getter
@Setter
@EruptI18n
public class BiReleaseModal extends BaseModel {

    @EruptField(
            edit = @Edit(title = "Menu Name", notNull = true)
    )
    private String name;

    @EruptField(
            edit = @Edit(
                    search = @Search,
                    title = "Menu Location", desc = "Skip if publishing to root directory", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id"),
                    filter = @Filter(conditionHandler = EruptMenuViewFilter.class)
            )
    )
    private EruptMenu eruptMenu;


}
