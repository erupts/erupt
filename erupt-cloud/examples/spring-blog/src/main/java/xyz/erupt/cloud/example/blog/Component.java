package xyz.erupt.cloud.example.blog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CalloutType;
import xyz.erupt.annotation.sub_field.sub_edit.GroupType;
import xyz.erupt.annotation.sub_field.sub_edit.KeyValueType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Map;
import java.util.Set;

/**
 * Every component that needs no extra endpoint (erupt-sdk SHOWCASE.md): layout fields are {@code @Transient},
 * multi choices and key-values are JSON columns.
 */
@Entity
@Table(name = "blog_component")
@Erupt(name = "Component")
public class Component extends BaseModel {

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search))
    private String name;

    @Transient
    @EruptField(edit = @Edit(title = "Guide", type = EditType.CALLOUT,
            calloutType = @CalloutType(value = "Every component a node can declare without extra endpoints.", style = CalloutType.Style.INFO)))
    private String intro;

    @Transient
    @EruptField(edit = @Edit(title = "Basic", type = EditType.DIVIDE))
    private String basic;

    @EruptField(edit = @Edit(title = "Code", type = EditType.HIDDEN))
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = 1000)
    @EruptField(views = @View(title = "Roles"), edit = @Edit(title = "Roles", type = EditType.MULTI_CHOICE,
            multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.CHECKBOX, vl = {
                    @VL(value = "ADMIN", label = "Admin"), @VL(value = "USER", label = "User"), @VL(value = "GUEST", label = "Guest")})))
    private Set<String> roles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = 1000)
    @EruptField(views = @View(title = "Languages"), edit = @Edit(title = "Languages", type = EditType.MULTI_CHOICE,
            multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT, vl = {
                    @VL(value = "JAVA", label = "Java"), @VL(value = "PYTHON", label = "Python"),
                    @VL(value = "GO", label = "Go"), @VL(value = "RUST", label = "Rust")})))
    private Set<String> langs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = 2000)
    @EruptField(views = @View(title = "Attributes"), edit = @Edit(title = "Attributes", type = EditType.KEY_VALUE,
            keyValueType = @KeyValueType(keyPlaceholder = "Key", valuePlaceholder = "Value")))
    private Map<String, String> attrs;

    @Transient
    @EruptField(edit = @Edit(title = "", type = EditType.EMPTY))
    private String spacer;

    @Transient
    @EruptField(edit = @Edit(title = "Extra", type = EditType.DIVIDE))
    private String extra;

    @Column(length = 1000)
    @EruptField(views = @View(title = "Location"), edit = @Edit(title = "Location", type = EditType.MAP))
    private String location;

    @Column(length = 100000)
    @EruptField(edit = @Edit(title = "Signature", type = EditType.SIGNATURE))
    private String signature;

    @Transient
    @EruptField(edit = @Edit(title = "Place & Sign", type = EditType.GROUP, groupType = @GroupType(fields = {"location", "signature"})))
    private String place;

}
