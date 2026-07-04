package xyz.erupt.upms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_post")
@Erupt(name = "Post Management", orderBy = "weight desc")
@EruptI18n
@Getter
@Setter
public class EruptPost extends BaseModel {

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "Post Code", sortable = true),
            edit = @Edit(title = "Post Code", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String code;

    @EruptField(
            views = @View(title = "Post Name", sortable = true),
            edit = @Edit(title = "Post Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Post Weight"),
            edit = @Edit(title = "Post Weight", desc = "Higher value means higher post level")
    )
    private Integer weight;

}
