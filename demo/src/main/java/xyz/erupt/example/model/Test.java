package xyz.erupt.example.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.*;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2020-02-29
 */
@Erupt(name = "demo", orderBy = "number desc")
@Table(name = "TEST")
@Entity
public class Test extends BaseModel {

    @EruptField(
            views = @View(title = "文本"),
            edit = @Edit(title = "文本", search = @Search(value = true, vague = true))
    )
    private String input = "默认文本";

    @EruptField(
            views = @View(title = "数字"),
            edit = @Edit(title = "数字", search = @Search(value = true, vague = true))
    )
    private Integer number;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "多对一表格", search = @Search(vague = true), type = EditType.REFERENCE_TABLE)
    )
    private Demo demo;

    @Lob
    @EruptField(
            views = @View(title = "地图"),
            edit = @Edit(title = "地图", type = EditType.MAP, mapType = @MapType(draw = true))
    )
    private String map;

    @EruptField(
            views = @View(title = "date"),
            edit = @Edit(title = "date", type = EditType.DATE, dateType = @DateType(type = DateType.Type.DATE))
    )
    private Date date;

    @EruptField(
            views = @View(title = "dateTime"),
            edit = @Edit(title = "dateTime", type = EditType.DATE, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date dateTime;

    @EruptField(
            views = @View(title = "time"),
            edit = @Edit(title = "time", type = EditType.DATE, dateType = @DateType(type = DateType.Type.TIME))
    )
    private String time;

    @EruptField(
            views = @View(title = "month"),
            edit = @Edit(title = "month", type = EditType.DATE, dateType = @DateType(type = DateType.Type.MONTH))
    )
    private String month;

    @EruptField(
            views = @View(title = "week"),
            edit = @Edit(title = "week", type = EditType.DATE, dateType = @DateType(type = DateType.Type.WEEK))
    )
    private String week;

    @EruptField(
            views = @View(title = "year"),
            edit = @Edit(title = "year", type = EditType.DATE, dateType = @DateType(type = DateType.Type.YEAR))
    )
    private String year;

    @Transient
    @EruptField(
            edit = @Edit(title = "地区选择 TEST", type = EditType.DIVIDE)
    )
    private String region;

    @ManyToOne
    @EruptField(
            views = @View(title = "省份", column = "name"),
            edit = @Edit(title = "省份", notNull = true, type = EditType.REFERENCE_TREE,
                    filter = @Filter("BaseArea.level = 1"))
    )
    private BaseArea province;

    @ManyToOne
    @EruptField(
            views = @View(title = "市", column = "name"),
            edit = @Edit(title = "市", notNull = true, type = EditType.REFERENCE_TREE,
                    filter = @Filter("BaseArea.level = 2"),
                    referenceTreeType = @ReferenceTreeType(dependField = "province", dependColumn = "pid.id")
            )
    )
    private BaseArea city;

    @ManyToOne
    @EruptField(
            views = @View(title = "区", column = "name"),
            edit = @Edit(title = "区", notNull = true, type = EditType.REFERENCE_TREE,
                    filter = @Filter("BaseArea.level = 3"),
                    referenceTreeType = @ReferenceTreeType(dependField = "city", dependColumn = "pid.id")
            )
    )
    private BaseArea area;

    @ManyToOne
    @EruptField(
            views = @View(title = "街道", column = "name"),
            edit = @Edit(title = "街道", notNull = true, type = EditType.REFERENCE_TREE,
                    filter = @Filter("BaseArea.level = 4"),
                    referenceTreeType = @ReferenceTreeType(dependField = "area", dependColumn = "pid.id")
            )
    )
    private BaseArea street;

    @ManyToOne
    @EruptField(
            views = @View(title = "社区", column = "name"),
            edit = @Edit(title = "社区", type = EditType.REFERENCE_TREE,
                    filter = @Filter("BaseArea.level = 5"),
                    referenceTreeType = @ReferenceTreeType(dependField = "street", dependColumn = "pid.id")
            )
    )
    private BaseArea community;


    @Transient
    @EruptField(
            edit = @Edit(title = "地区选择 TABLE TEST", type = EditType.DIVIDE)
    )
    private String regiont;

    @ManyToOne
    @EruptField(
            views = @View(title = "省份", column = "name"),
            edit = @Edit(title = "省份", notNull = true, type = EditType.REFERENCE_TABLE,
                    filter = @Filter("BaseArea.level = 1"))
    )
    private BaseArea provincet;

    @ManyToOne
    @EruptField(
            views = @View(title = "市", column = "name"),
            edit = @Edit(title = "市", notNull = true, type = EditType.REFERENCE_TABLE,
                    filter = @Filter("BaseArea.level = 2"),
                    referenceTableType = @ReferenceTableType(dependField = "provincet", dependColumn = "pid.id")
            )
    )
    private BaseArea cityt;

    @ManyToOne
    @EruptField(
            views = @View(title = "区", column = "name"),
            edit = @Edit(title = "区", notNull = true, type = EditType.REFERENCE_TABLE,
                    filter = @Filter("BaseArea.level = 3"),
                    referenceTableType = @ReferenceTableType(dependField = "cityt", dependColumn = "pid.id")
            )
    )
    private BaseArea areat;

    @ManyToOne
    @EruptField(
            views = @View(title = "街道", column = "name"),
            edit = @Edit(title = "街道", notNull = true, type = EditType.REFERENCE_TABLE,
                    filter = @Filter("BaseArea.level = 4"),
                    referenceTableType = @ReferenceTableType(dependField = "areat", dependColumn = "pid.id")
            )
    )
    private BaseArea streett;

    @ManyToOne
    @EruptField(
            views = @View(title = "社区", column = "name"),
            edit = @Edit(title = "社区", type = EditType.REFERENCE_TABLE,
                    filter = @Filter("BaseArea.level = 5"),
                    referenceTableType = @ReferenceTableType(dependField = "streett", dependColumn = "pid.id")
            )
    )
    private BaseArea communityt;
}
