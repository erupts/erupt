package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates @Drill data drill-down: clicking a row drills into DrillDetailModel,
 * filtered by id → orderId.
 */
@Getter
@Setter
@Entity
@Erupt(name = "Drill - Order List",
        drills = {
                @Drill(
                        code = "items",
                        title = "View Items",
                        icon = "fa fa-list",
                        link = @Link(
                                linkErupt = DrillDetailModel.class,
                                joinColumn = "orderId"
                        )
                )
        }
)
public class DrillModel extends BaseModel {

    @EruptField(
            views = @View(title = "Order No"),
            edit = @Edit(title = "Order No", notNull = true)
    )
    private String orderNo;

    @EruptField(
            views = @View(title = "Customer"),
            edit = @Edit(title = "Customer")
    )
    private String customer;

    @EruptField(
            views = @View(title = "Total Amount"),
            edit = @Edit(title = "Total Amount")
    )
    private Double totalAmount;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status")
    )
    private String status;
}
