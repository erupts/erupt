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
@Table(name = "e_bi_group", uniqueConstraints = @UniqueConstraint(name = "uk_bi_group_code", columnNames = "code"))
@Erupt(name = "Group Manager", tree = @Tree(pid = "parent.id"), dataProxy = BiGroup.class)
@Getter
@Setter
@Component
@EruptI18n
public class BiGroup extends MetaModelUpdateVo implements DataProxy<BiGroup> {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", sortable = true),
            edit = @Edit(title = "Code", notNull = true, search = @Search, readonly = @Readonly(add = false))
    )
    private String code;

    @EruptField(
            views = @View(title = "Group Name", sortable = true),
            edit = @Edit(title = "Group Name", notNull = true, search = @Search)
    )
    private String name;

    @ManyToOne
    @EruptField(
            views = @View(title = "Parent Group", column = "name"),
            edit = @Edit(title = "Parent Group", type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"))
    )
    private BiGroup parent;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark", sortable = true),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public void addBehavior(BiGroup biGroup) {
        biGroup.setCode(Erupts.generateCode());
    }

}
