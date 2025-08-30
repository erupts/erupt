package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_post")
@Erupt(name = "岗位管理", orderBy = "weight desc")
@EruptI18n
@Getter
@Setter
public class EruptPost extends BaseModel {

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "岗位编码", sortable = true),
            edit = @Edit(title = "岗位编码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "岗位名称", sortable = true),
            edit = @Edit(title = "岗位名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "岗位权重"),
            edit = @Edit(title = "岗位权重", desc = "数值越高，岗位级别越高")
    )
    private Integer weight;

}
