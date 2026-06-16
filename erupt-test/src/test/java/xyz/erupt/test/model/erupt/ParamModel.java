package xyz.erupt.test.model.erupt;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.KV;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Demonstrates @Erupt param (KV extension params):
 * custom key-value params are accessible via DataProxyContext.get() in a dataProxy,
 * or used as variables in frontend templates (TPL).
 */
@Getter
@Setter
@Entity
@Erupt(name = "Param (KV Extension) Demo",
        param = {
                @KV(key = "module", value = "inventory", desc = "Module identifier"),
                @KV(key = "version", value = "v2", desc = "API version"),
                @KV(key = "tenantId", value = "1001", desc = "Tenant isolation key")
        }
)
public class ParamModel extends BaseModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Code"),
            edit = @Edit(title = "Code")
    )
    private String code;

    @EruptField(
            views = @View(title = "Amount"),
            edit = @Edit(title = "Amount")
    )
    private Double amount;
}
