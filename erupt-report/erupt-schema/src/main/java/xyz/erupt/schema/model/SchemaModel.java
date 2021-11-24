package xyz.erupt.schema.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/11/23 23:28
 */
@Erupt(
        name = "amis config",
        rowOperation = @RowOperation(
                title = "发布",
                mode = RowOperation.Mode.SINGLE, icon = "fa fa-send",
                operationHandler = SchemaModel.class
        )
)
@Entity
@Table(name = "e_schema", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Getter
@Setter
public class SchemaModel extends BaseModel implements OperationHandler<SchemaModel, Void> {

    @EruptField(
            views = @View(title = "页面编码"),
            edit = @Edit(title = "页面编码", search = @Search(vague = true), notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "页面名称"),
            edit = @Edit(title = "页面名称", search = @Search(vague = true), notNull = true)
    )
    private String name;

    @Lob
    @EruptField(
            views = @View(title = "配置"),
            edit = @Edit(title = "配置", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"),
                    notNull = true)
    )
    private String json = "{}";

    @Override
    public String exec(List<SchemaModel> data, Void unused, String[] param) {
        return null;
    }
}
