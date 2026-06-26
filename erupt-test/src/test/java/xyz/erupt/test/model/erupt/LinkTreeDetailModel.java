package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates left-tree-right-table: the right-side table (product list).
 * linkTree.field points to the field in this model that holds the tree node ID.
 * dependNode=true means data is shown only when a tree node is selected.
 */
@Getter
@Setter
@Entity
@Erupt(name = "LinkTree - Product List",
        linkTree = @LinkTree(field = "categoryId", dependNode = true)
)
public class LinkTreeDetailModel extends BaseModel {

    @EruptField(
            views = @View(title = "Product Name"),
            edit = @Edit(title = "Product Name", notNull = true)
    )
    private String productName;

    @EruptField(
            views = @View(title = "Category ID"),
            edit = @Edit(title = "Category ID")
    )
    private Long categoryId;

    @EruptField(
            views = @View(title = "Price"),
            edit = @Edit(title = "Price")
    )
    private Double price;

    @EruptField(
            views = @View(title = "Stock"),
            edit = @Edit(title = "Stock")
    )
    private Integer stock;
}
