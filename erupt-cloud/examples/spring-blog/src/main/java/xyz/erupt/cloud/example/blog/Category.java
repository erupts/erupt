package xyz.erupt.cloud.example.blog;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Entity
@Table(name = "blog_category")
@Erupt(name = "Category", orderBy = "Category.sort asc", tree = @Tree(pid = "parent.id"))
public class Category extends BaseModel {

    @EruptField(views = @View(title = "Name", sortable = true), edit = @Edit(title = "Name", notNull = true, search = @Search))
    private String name;

    // JPA holds the parent as an entity; the SDK nodes keep a plain "parent" key column instead
    @ManyToOne
    @EruptField(views = @View(title = "Parent", column = "name"),
            edit = @Edit(title = "Parent", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parent.id")))
    private Category parent;

    @EruptField(views = @View(title = "Sort", sortable = true), edit = @Edit(title = "Sort"))
    private Integer sort;

}
