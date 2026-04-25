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
 * 演示 dataProxy：绑定 TestDataProxy，在新增/修改/删除各阶段执行业务逻辑
 */
@Getter
@Setter
@Entity
@Erupt(name = "DataProxy Demo",
        dataProxy = {TestDataProxy.class}
)
public class DataProxyModel extends BaseModel {

    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "Content"),
            edit = @Edit(title = "Content")
    )
    private String content;

    @EruptField(
            views = @View(title = "Creator"),
            edit = @Edit(title = "Creator")
    )
    private String creator;

    @EruptField(
            views = @View(title = "Status"),
            edit = @Edit(title = "Status")
    )
    private String status;
}
