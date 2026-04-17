package xyz.erupt.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "MultiChoiceEdit")
public class MultiChoiceModel extends BaseModel {

    // 多选框组（默认 CHECKBOX）
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Roles"),
            edit = @Edit(title = "Roles", type = EditType.MULTI_CHOICE,
                    multiChoiceType = @MultiChoiceType(vl = {
                            @VL(value = "ADMIN", label = "Admin"),
                            @VL(value = "USER", label = "User"),
                            @VL(value = "GUEST", label = "Guest")
                    }),
                    search = @Search)
    )
    private String roles;

    // 下拉多选（SELECT 布局）
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Tags"),
            edit = @Edit(title = "Tags", type = EditType.MULTI_CHOICE,
                    multiChoiceType = @MultiChoiceType(
                            type = MultiChoiceType.Type.SELECT,
                            vl = {
                                    @VL(value = "JAVA", label = "Java"),
                                    @VL(value = "PYTHON", label = "Python"),
                                    @VL(value = "GO", label = "Go"),
                                    @VL(value = "RUST", label = "Rust")
                            }))
    )
    private String tags;

    // 动态 handler 获取选项
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Permissions"),
            edit = @Edit(title = "Permissions", type = EditType.MULTI_CHOICE,
                    multiChoiceType = @MultiChoiceType(
                            type = MultiChoiceType.Type.SELECT,
                            fetchHandler = {TestChoiceFetchHandler.class}))
    )
    private String permissions;
}
