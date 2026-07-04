package xyz.erupt.upms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_user")
@Erupt(
        name = "User Management"
)
@EruptI18n
@Getter
@Setter
public class EruptUserPostVo extends BaseModel {

    @EruptField(
            views = @View(title = "name", sortable = true),
            edit = @Edit(title = "name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @ManyToOne
    @EruptField(
            views = @View(title = "Org", column = "name"),
            edit = @Edit(title = "Org", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id"))
    )
    private EruptOrg eruptOrg;

    @ManyToOne
    @EruptField(
            views = @View(title = "Post", column = "name"),
            edit = @Edit(title = "Post", type = EditType.REFERENCE_TREE)
    )
    private EruptPost eruptPost;

    public EruptUserPostVo() {
    }

    public EruptUserPostVo(Long id) {
        this.setId(id);
    }

}
