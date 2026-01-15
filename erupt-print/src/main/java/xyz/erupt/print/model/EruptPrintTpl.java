package xyz.erupt.print.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.HtmlEditorType;
import xyz.erupt.upms.helper.HyperModelUpdateVo;

@Getter
@Setter
@Entity
@Table(name = "e_print_tpl")
@Erupt(name = "Print Template")
public class EruptPrintTpl extends HyperModelUpdateVo {

    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "内容"),
            edit = @Edit(
                    title = "内容", type = EditType.HTML_EDITOR, notNull = true,
                    htmlEditorType = @HtmlEditorType(HtmlEditorType.Type.CKEDITOR)
            )
    )
    private String content;

}
