package xyz.erupt.bi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_group", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "分组管理", tree = @Tree(pid = "parent.id"), dataProxy = BiGroup.class)
@Getter
@Setter
@Component
@EruptI18n
public class BiGroup extends MetaModelUpdateVo implements DataProxy<BiGroup> {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "编码", sortable = true),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true), readonly = @Readonly(add = false))
    )
    private String code;

    @EruptField(
            views = @View(title = "组别名称", sortable = true),
            edit = @Edit(title = "组别名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @ManyToOne
    @EruptField(
            views = @View(title = "上级组别", column = "name"),
            edit = @Edit(title = "上级组别", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"))
    )
    private BiGroup parent;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "备注", sortable = true),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public void addBehavior(BiGroup biGroup) {
        biGroup.setCode(Erupts.generateCode());
    }

}
