package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.*;

/**
 * 管理报表元数据
 * @author YuePeng
 * date 2022/7/19 07:19
 */
@Entity
@Table(name = "e_bi_view_meta", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "报表配置", linkTree = @LinkTree(field = "biGroup"), dataProxy = BiViewMeta.class)
@Getter
@Setter
@Component
@EruptI18n
public class BiViewMeta extends MetaModelUpdateVo implements DataProxy<BiViewMeta> {

    @EruptField(
            views = @View(title = "编码", width = "100px"),
            edit = @Edit(title = "编码", search = @Search, show = false)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "bi_group_id")
    @EruptField(
            views = @View(title = "组别", column = "name", sortable = true),
            edit = @Edit(title = "组别", notNull = true, type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"), search = @Search)
    )
    private BiGroup biGroup;

    @ManyToOne
    @JoinColumn(name = "datasource_id")
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

    @EruptField(
            views = @View(title = "缓存时间", width = "100px", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "缓存时间（秒）")
    )
    private Integer cacheTime = 1;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "查询表达式"),
            edit = @Edit(title = "查询表达式", notNull = true, type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String express;

    @Override
    public void beforeAdd(BiViewMeta biViewMeta) {
        biViewMeta.setCode(Erupts.generateCode());
    }
}
