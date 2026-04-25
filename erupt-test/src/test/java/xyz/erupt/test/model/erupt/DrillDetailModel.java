package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * 演示 @Drill 下钻目标表：与 DrillModel 通过 orderId 关联
 */
@Getter
@Setter
@Entity
@Erupt(name = "Drill - Order Item")
public class DrillDetailModel extends BaseModel {

    @EruptField(
            views = @View(title = "Order ID"),
            edit = @Edit(title = "Order ID")
    )
    private Long orderId;

    @EruptField(
            views = @View(title = "Product Name"),
            edit = @Edit(title = "Product Name", notNull = true)
    )
    private String productName;

    @EruptField(
            views = @View(title = "Quantity"),
            edit = @Edit(title = "Quantity")
    )
    private Integer quantity;

    @EruptField(
            views = @View(title = "Price"),
            edit = @Edit(title = "Price")
    )
    private Double price;
}
