package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "ReferenceTreeEdit")
public class ReferenceTreeModel extends BaseModel {

    // 基础树引用（展开全部层级）
    @ManyToOne
    @EruptField(
            views = @View(title = "Category", column = "name"),
            edit = @Edit(title = "Category", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(id = "id", label = "name"),
                    search = @Search)
    )
    private RefTargetModel category;

    // 只展开第一级
    @ManyToOne
    @EruptField(
            views = @View(title = "Department", column = "name"),
            edit = @Edit(title = "Department", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(
                            id = "id", label = "name",
                            expandLevel = 1))
    )
    private RefTargetModel department;

    // 按 name 排序
    @ManyToOne
    @EruptField(
            views = @View(title = "Region", column = "name"),
            edit = @Edit(title = "Region", type = EditType.REFERENCE_TREE,
                    orderBy = "name asc",
                    referenceTreeType = @ReferenceTreeType(id = "id", label = "name"))
    )
    private RefTargetModel region;
}
