package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2020-03-30
 */
@Entity
@Table(name = "E_BI_DIMENSION_REFERENCE")
@Erupt(name = "参照维度")
@Getter
@Setter
public class BiDimensionReference extends BaseModel {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search)
    )
    private String name;

    @ManyToOne
    @EruptField(
            views = @View(title = "数据源", column = "name"),
            edit = @Edit(title = "数据源", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private BiDataSource dataSource;

    @ManyToOne
    @EruptField(
            views = @View(title = "处理类", column = "name"),
            edit = @Edit(title = "处理类", type = EditType.REFERENCE_TABLE)
    )
    private BiClassHandler classHandler;


    @Lob
    @EruptField(
            views = @View(title = "参照SQL"),
            edit = @Edit(title = "参照SQL", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql"), notNull = true)
    )
    private String refSql;
}
