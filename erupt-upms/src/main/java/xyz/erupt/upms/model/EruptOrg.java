package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2020-04-08
 */
@Entity
@Table(name = "e_upms_org")
@Erupt(
        name = "Org Management",
        tree = @Tree(pid = "parentOrg.id", expandLevel = 5),
        orderBy = "EruptOrg.sort asc"
)
@EruptI18n
@Getter
@Setter
@NoArgsConstructor
public class EruptOrg extends BaseModel {

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "Org Code", sortable = true),
            edit = @Edit(title = "Org Code", notNull = true, search = @Search)
    )
    private String code;

    @EruptField(
            views = @View(title = "Org Name", sortable = true),
            edit = @Edit(title = "Org Name", notNull = true, search = @Search)
    )
    private String name;

    @ManyToOne
    @EruptField(
            edit = @Edit(
                    title = "Parent Org",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id", expandLevel = 3)
            )
    )
    private EruptOrg parentOrg;

    @EruptField(
            edit = @Edit(
                    title = "Display Order"
            )
    )
    private Integer sort;


}
