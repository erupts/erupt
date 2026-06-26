package xyz.erupt.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Erupt(name = "MultiChoiceEdit")
public class MultiChoiceModel extends BaseModel {

    // checkbox group (default CHECKBOX)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
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
    private Set<String> roles;

    // multi-select dropdown (SELECT layout)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
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
    private List<String> tags;

    // dynamic handler for options
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Permissions"),
            edit = @Edit(title = "Permissions", type = EditType.MULTI_CHOICE,
                    multiChoiceType = @MultiChoiceType(
                            type = MultiChoiceType.Type.SELECT,
                            fetchHandler = {TestChoiceFetchHandler.class}))
    )
    private List<String> permissions;
}
