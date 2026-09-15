package xyz.erupt.sample.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.KeyValueType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Date;
import java.util.Map;

@Erupt(name = "DEMO", dataProxy = DemoDataProxy.class, power = @Power(export = true, importable = true))
@Table(name = "t_demo")
@Entity
@Getter
@Setter
public class Demo extends BaseModel {

    @EruptField(
            views = @View(title = "Text"),
            edit = @Edit(title = "Text", search = @Search, onchange = DemoInputChange.class)
    )
    private String input;

    @EruptField(
            views = @View(title = "Text 2"),
            edit = @Edit(title = "Text 2", readonly = @Readonly)
    )
    private String input2;

    @EruptField(
            views = @View(title = "Number", sortable = true),
            edit = @Edit(title = "Number", search = @Search)
    )
    private Integer number = 100;  // Default value 100

    @EruptField(
            views = @View(title = "Float", sortable = true),
            edit = @Edit(title = "Float", search = @Search)
    )
    private Double dou = 100.1111D;  // Default value 100

    @EruptField(
            views = @View(title = "Bool"),
            edit = @Edit(title = "Bool", search = @Search)
    )
    private Boolean bool;

    @EruptField(
            views = @View(title = "Date"),
            edit = @Edit(title = "Date", search = @Search)
    )
    private Date date;

    // TEMP-PROBE: exercises the cell editors for tag, color and slider columns
    @EruptField(views = @View(title = "Tags"), edit = @Edit(title = "Tags", type = EditType.TAGS))
    private String tagsVal;

    @EruptField(views = @View(title = "Color", type = ViewType.COLOR),
            edit = @Edit(title = "Color", type = EditType.COLOR))
    private String colorVal;

    @EruptField(views = @View(title = "Progress", type = ViewType.PROGRESS),
            edit = @Edit(title = "Progress", type = EditType.SLIDER))
    private Integer progressVal;

    @JdbcTypeCode(SqlTypes.JSON)
    @EruptField(views = @View(title = "Params"),
            edit = @Edit(title = "Params", type = EditType.KEY_VALUE,
                    keyValueType = @KeyValueType(keys = {"timeout", "retry", "region"})))
    private Map<String, String> paramsVal;



}
