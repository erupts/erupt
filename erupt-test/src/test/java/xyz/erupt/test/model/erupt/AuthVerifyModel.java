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
 * 演示 authVerify = false：跳过权限校验，任何人均可访问
 * 适用于公开数据接口场景（如字典表、公告等）
 */
@Getter
@Setter
@Entity
@Erupt(name = "AuthVerify - Public Access",
        authVerify = false
)
public class AuthVerifyModel extends BaseModel {

    @EruptField(
            views = @View(title = "Key"),
            edit = @Edit(title = "Key", notNull = true)
    )
    private String key;

    @EruptField(
            views = @View(title = "Value"),
            edit = @Edit(title = "Value", notNull = true)
    )
    private String value;

    @EruptField(
            views = @View(title = "Description"),
            edit = @Edit(title = "Description")
    )
    private String description;
}
