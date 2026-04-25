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
 * 演示左树右表之——右侧表格（商品列表）
 * linkTree.field 指向本模型中关联树节点 ID 的字段
 * dependNode=true 表示必须选中树节点才展示数据
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
