package xyz.erupt.bi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2020-03-30
 */
@Entity
@Table(name = "e_bi_dimension_reference")
@Erupt(name = "参照维度")
@Getter
@Setter
@EruptI18n
public class BiDimensionReference extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "名称", sortable = true),
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
            edit = @Edit(title = "处理类", type = EditType.REFERENCE_TABLE)
    )
    private BiClassHandler classHandler;


    @Column(columnDefinition = "text")
    @EruptField(
            views = @View(title = "参照SQL"),
            edit = @Edit(title = "参照SQL", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql"), notNull = true)
    )
    private String refSql;
}
