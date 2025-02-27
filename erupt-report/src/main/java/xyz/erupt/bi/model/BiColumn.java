package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
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
import xyz.erupt.annotation.sub_field.sub_edit.ShowBy;
import xyz.erupt.bi.constant.ColumnType;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author YuePeng
 * date 2022/3/12 00:53
 */
@Entity
@Table(name = "e_bi_column")
@Erupt(name = "列配置", dataProxy = BiColumn.class)
@Getter
@Setter
@Component
@EruptI18n
public class BiColumn extends BaseModel implements DataProxy<BiColumn> {

    @EruptField(
            views = @View(title = "列名", sortable = true),
            edit = @Edit(title = "列名", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "类型", sortable = true),
            edit = @Edit(title = "类型", type = EditType.CHOICE, notNull = true, choiceType = @ChoiceType(fetchHandler = ColumnType.Fetch.class))
    )
    private String type = ColumnType.STRING.getCode();

    @EruptField(
            views = @View(title = "宽度", sortable = true),
            edit = @Edit(title = "宽度")
    )
    private Integer width;

    @EruptField(
            views = @View(title = "是否显示", sortable = true),
            edit = @Edit(title = "是否显示", notNull = true)
    )
    private Boolean display = true;

    @EruptField(
            views = @View(title = "是否排序", sortable = true),
            edit = @Edit(title = "是否排序", notNull = true)
    )
    private Boolean sortable = true;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            edit = @Edit(title = "下钻SQL", type = EditType.CODE_EDITOR,
                    showBy = @ShowBy(dependField = "type", expr = "value == 'drill'"),
                    codeEditType = @CodeEditorType(language = "sql"))
    )
    private String drillExpress;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "描述", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public void beforeAdd(BiColumn biColumn) {
        if (ColumnType.DRILL.equals(biColumn.type)) {
            if (StringUtils.isBlank(biColumn.drillExpress)) {
                throw new EruptWebApiRuntimeException("下钻SQL必填");
            }
        }
    }

    @Override
    public void beforeUpdate(BiColumn biColumn) {
        this.beforeAdd(biColumn);
    }

}
