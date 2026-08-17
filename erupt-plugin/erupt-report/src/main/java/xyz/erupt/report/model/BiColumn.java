package xyz.erupt.report.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.report.constant.ColumnType;

/**
 * @author YuePeng
 * date 2022/3/12 00:53
 */
@Entity
@Table(name = "e_bi_column")
@Erupt(name = "Column Config", dataProxy = BiColumn.class)
@Getter
@Setter
@Component
@EruptI18n
public class BiColumn extends BaseModel implements DataProxy<BiColumn> {

    @EruptField(
            views = @View(title = "Column Name", sortable = true),
            edit = @Edit(title = "Column Name", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "Type", sortable = true),
            edit = @Edit(title = "Type", type = EditType.CHOICE, notNull = true, choiceType = @ChoiceType(fetchHandler = ColumnType.Fetch.class))
    )
    private String type = ColumnType.STRING.getCode();

    @EruptField(
            views = @View(title = "Width", sortable = true),
            edit = @Edit(title = "Width")
    )
    private Integer width;

    @EruptField(
            views = @View(title = "Display", sortable = true),
            edit = @Edit(title = "Display", notNull = true)
    )
    private Boolean display = true;

    @EruptField(
            views = @View(title = "Sortable", sortable = true),
            edit = @Edit(title = "Sortable", notNull = true)
    )
    private Boolean sortable = true;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "Drill SQL", type = EditType.CODE_EDITOR,
                    dynamic = @Dynamic(dependField = "type", condition = "value == 'drill'", match = Dynamic.Ctrl.NOTNULL),
                    codeEditType = @CodeEditorType(language = "sql"))
    )
    private String drillExpress;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "Description", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public void beforeAdd(BiColumn biColumn) {
        if (ColumnType.DRILL == ColumnType.valueOf(biColumn.type)) {
            if (StringUtils.isBlank(biColumn.drillExpress)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("bi.drill_sql_required"));
            }
        }
    }

    @Override
    public void beforeUpdate(BiColumn biColumn) {
        this.beforeAdd(biColumn);
    }

}
