package xyz.erupt.test.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "CodeEditorEdit")
public class CodeEditorModel extends BaseModel {

    // JSON (default height 300)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "JSON Config"),
            edit = @Edit(title = "JSON Config", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "json"))
    )
    private String jsonConfig;

    // SQL (height 500)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "SQL Script"),
            edit = @Edit(title = "SQL Script", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql", height = 500))
    )
    private String sqlScript;

    // XML
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "XML Content"),
            edit = @Edit(title = "XML Content", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "xml", height = 400))
    )
    private String xmlContent;

    // JavaScript
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "JS Script"),
            edit = @Edit(title = "JS Script", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "javascript", height = 400))
    )
    private String jsScript;

    // YAML
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "YAML Config"),
            edit = @Edit(title = "YAML Config", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "yaml"))
    )
    private String yamlConfig;
}
