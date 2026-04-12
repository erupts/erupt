package xyz.erupt.cloud.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModel;

/**
 * @author YuePeng
 * date 2021/12/16 00:32
 */
@Getter
@Setter
@Entity
@Table(name = "e_cloud_node_group")
@Erupt(name = "Group Config")
@EruptI18n
public class CloudNodeGroup extends MetaModel {

    @Column(unique = true)
    @EruptField(
            views = @View(title = "Code", sortable = true),
            edit = @Edit(title = "Code", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Group Config"),
            edit = @Edit(title = "Group Config", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String config;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Description", type = ViewType.HTML),
            edit = @Edit(title = "Description", type = EditType.TEXTAREA)
    )
    private String remark;

}
