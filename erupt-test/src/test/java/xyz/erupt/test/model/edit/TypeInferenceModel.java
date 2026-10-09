package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.test.model.erupt.TreeModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every field here leaves the edit type on AUTO: the Java type alone must pick the component.
 */
@Getter
@Setter
@Entity
@Table(name = "e_test_type_inference")
@Erupt(name = "Type Inference")
public class TypeInferenceModel extends BaseModel {

    @EruptField(views = @View(title = "Created"), edit = @Edit(title = "Created"))
    private LocalDateTime created;

    @EruptField(views = @View(title = "Day"), edit = @Edit(title = "Day"))
    private LocalDate day;

    @EruptField(views = @View(title = "Clock"), edit = @Edit(title = "Clock"))
    private LocalTime clock;

    @Temporal(TemporalType.DATE)
    @EruptField(views = @View(title = "Birthday"), edit = @Edit(title = "Birthday"))
    private Date birthday;

    @EruptField(views = @View(title = "Stamp"), edit = @Edit(title = "Stamp"))
    private Date stamp;

    @ManyToOne
    @EruptField(views = @View(title = "Kind", column = "plain"), edit = @Edit(title = "Kind"))
    private EnumColumnModel kind;

    @ManyToOne
    @EruptField(views = @View(title = "Node", column = "name"), edit = @Edit(title = "Node"))
    private TreeModel node;

    @OneToMany
    @EruptField(views = @View(title = "Items"), edit = @Edit(title = "Items"))
    private List<EnumColumnModel> items;

    @ManyToMany
    @EruptField(views = @View(title = "Links"), edit = @Edit(title = "Links"))
    private List<TreeModel> links;

    @JdbcTypeCode(SqlTypes.JSON)
    @EruptField(views = @View(title = "Kinds"), edit = @Edit(title = "Kinds"))
    private Set<EnumColumnModel.Kind> kinds;

    @JdbcTypeCode(SqlTypes.JSON)
    @EruptField(views = @View(title = "Attrs"), edit = @Edit(title = "Attrs"))
    private Map<String, String> attrs;

}
