package xyz.erupt.example.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2019-10-08.
 */
@Erupt(name = "demo_sub")
@Table(name = "DEMO")
@Entity
@Component
public class DemoSub extends BaseModel {


    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称")
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "ERUPT_MENU_ID")
    @EruptField(
            views = @View(title = "菜单", column = "name"),
            edit = @Edit(
                    title = "菜单",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id")
            )
    )
    private EruptMenu eruptMenu;

}
