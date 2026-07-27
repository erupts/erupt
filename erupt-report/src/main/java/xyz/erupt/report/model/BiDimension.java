package xyz.erupt.report.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_dimension", uniqueConstraints = @UniqueConstraint(name = "uk_bi_dimension_code_bi", columnNames = {"code", "bi_id"}))
@Erupt(name = "Query Dimension", dataProxy = BiDimension.class)
@Getter
@Setter
@EruptI18n
public class BiDimension extends BaseModel implements DataProxy<BiDimension> {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Dimension Code", sortable = true),
            edit = @Edit(title = "Dimension Code", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "Sort", sortable = true),
            edit = @Edit(title = "Sort")
    )
    private Integer sort;

    @EruptField(
            views = @View(title = "Required"),
            edit = @Edit(title = "Required", notNull = true)
    )
    private Boolean notNull = false;

    @ManyToOne
    @EruptField(
            views = @View(title = "Reference Dimension", column = "name"),
            edit = @Edit(title = "Reference Dimension", type = EditType.REFERENCE_TABLE,
                    dynamic = @Dynamic(dependField = "type",
                            condition = "value && value.indexOf('REFERENCE') != -1"))
    )
    private BiDimensionReference biDimensionReference;

    @EruptField(
            views = @View(title = "Dimension Type"),
            edit = @Edit(
                    title = "Dimension Type",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            type = ChoiceType.Type.RADIO,
                            vl = {
                                    @VL(value = "INPUT", label = "Text"),
                                    @VL(value = "TAG", label = "Tag"),

                                    @VL(value = "NUMBER", label = "Number"),
                                    @VL(value = "NUMBER_RANGE", label = "Number Range"),

                                    @VL(value = "DATE", label = "Date"),
                                    @VL(value = "TIME", label = "Time"),
                                    @VL(value = "DATETIME", label = "DateTime"),

                                    @VL(value = "WEEK", label = "Week"),
                                    @VL(value = "MONTH", label = "Month"),
                                    @VL(value = "YEAR", label = "Year"),

                                    @VL(value = "DATE_RANGE", label = "Date Range"),
                                    @VL(value = "DATETIME_RANGE", label = "DateTime Range"),

                                    @VL(value = "REFERENCE", label = "Single Reference", desc = "Returns two columns: id/label"),
                                    @VL(value = "REFERENCE_MULTI", label = "Multi Reference", desc = "Returns two columns: id/label"),
                                    @VL(value = "REFERENCE_RADIO", label = "Radio Reference", desc = "Returns two columns: id/label"),
                                    @VL(value = "REFERENCE_CHECKBOX", label = "Checkbox Reference", desc = "Returns two columns: id/label"),

                                    @VL(value = "REFERENCE_TREE_RADIO", label = "Single Tree Reference", desc = "Returns three columns: id/label/pid; empty pid = root"),
                                    @VL(value = "REFERENCE_TREE_MULTI", label = "Multi Tree Reference", desc = "Returns three columns: id/label/pid; empty pid = root"),
                                    @VL(value = "REFERENCE_CASCADE", label = "Cascade Reference", desc = "Returns three columns: id/label/pid; empty pid = root"),

                                    @VL(value = "REFERENCE_TABLE_RADIO", label = "Single Table Reference", desc = "Returns 2-N columns; first column is query key, not shown in frontend"),
                                    @VL(value = "REFERENCE_TABLE_MULTI", label = "Multi Table Reference", desc = "Returns 2-N columns; first column is query key, not shown in frontend"),
                            }
                    )
            )
    )
    private String type;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Default Value"),
            edit = @Edit(title = "Default Value", desc = "Generate default dynamically via JS; quote strings"
                    , type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "javascript", height = 80))
    )
    private String defaultValue;

    @Override
    public void beforeAdd(BiDimension biDimension) {
        if (biDimension.getType().startsWith("REFERENCE") && null == biDimension.getBiDimensionReference()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("bi.reference_required"));
        }
    }

    @Override
    public void beforeUpdate(BiDimension biDimension) {
        this.beforeAdd(biDimension);
    }

}

