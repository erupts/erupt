package xyz.erupt.cloud.server.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModel;

import javax.persistence.*;

/**
 * @author YuePeng
 * date 2021/12/16 00:32
 */
@Getter
@Setter
@Entity
@Table(name = "e_cloud_node_group", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "分组配置")
@EruptI18n
public class CloudNodeGroup extends MetaModel {

    @EruptField(
            views = @View(title = "编码", sortable = true),
            edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "分组配置"),
            edit = @Edit(title = "分组配置", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String config;

    @Column(length = 5000)
    @EruptField(
            views = @View(title = "描述", type = ViewType.HTML),
            edit = @Edit(title = "描述", type = EditType.TEXTAREA)
    )
    private String remark;

}
