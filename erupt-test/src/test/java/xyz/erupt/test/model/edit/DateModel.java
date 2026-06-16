package xyz.erupt.test.model.edit;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "DateEdit")
public class DateModel extends BaseModel {

    // date
    @EruptField(
            views = @View(title = "Date"),
            edit = @Edit(title = "Date", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.DATE),
                    search = @Search)
    )
    private String date;

    // time
    @EruptField(
            views = @View(title = "Time"),
            edit = @Edit(title = "Time", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.TIME))
    )
    private String time;

    // date + time
    @EruptField(
            views = @View(title = "DateTime"),
            edit = @Edit(title = "DateTime", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private String dateTime;

    // month
    @EruptField(
            views = @View(title = "Month"),
            edit = @Edit(title = "Month", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.MONTH))
    )
    private String month;

    // week
    @EruptField(
            views = @View(title = "Week"),
            edit = @Edit(title = "Week", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.WEEK))
    )
    private String week;

    // year
    @EruptField(
            views = @View(title = "Year"),
            edit = @Edit(title = "Year", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.YEAR))
    )
    private String year;

    // future dates only
    @EruptField(
            views = @View(title = "Expire Date"),
            edit = @Edit(title = "Expire Date", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.DATE, pickerMode = DateType.PickerMode.FUTURE))
    )
    private String expireDate;

    // past dates only
    @EruptField(
            views = @View(title = "Birth Date"),
            edit = @Edit(title = "Birth Date", type = EditType.DATE,
                    dateType = @DateType(type = DateType.Type.DATE, pickerMode = DateType.PickerMode.HISTORY))
    )
    private String birthDate;
}
