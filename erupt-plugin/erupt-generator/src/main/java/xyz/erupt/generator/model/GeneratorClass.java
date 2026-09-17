package xyz.erupt.generator.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.generator.base.SuperModel;
import xyz.erupt.generator.handler.CodeDownloadHandler;
import xyz.erupt.generator.handler.CodePreviewHandler;
import xyz.erupt.generator.handler.DbImportHandler;
import xyz.erupt.generator.handler.SuperModelChoice;
import xyz.erupt.generator.model.input.DbImportModal;
import xyz.erupt.jpa.model.MetaModel;

import java.util.Set;

@EruptI18n
@Erupt(name = "Generate Erupt Code",
        rowOperation = {
                @RowOperation(
                        code = "dbImport", title = "Import from Database", icon = "fa fa-database",
                        mode = RowOperation.Mode.BUTTON, callHint = "",
                        eruptClass = DbImportModal.class, operationHandler = DbImportHandler.class
                ),
                @RowOperation(
                        code = "preview", title = "Preview", icon = "fa fa-code",
                        mode = RowOperation.Mode.SINGLE, callHint = "",
                        operationHandler = CodePreviewHandler.class
                ),
                @RowOperation(
                        code = "download", title = "Download", icon = "fa fa-download",
                        mode = RowOperation.Mode.MULTI_ONLY, callHint = "",
                        operationHandler = CodeDownloadHandler.class
                )
        }
)
@Table(name = "e_generator_class")
@Entity
@Getter
@Setter
public class GeneratorClass extends MetaModel {

    @EruptField(
            views = @View(title = "Name"),
            edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Entity Class"),
            edit = @Edit(title = "Entity Class", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String className;

    @EruptField(
            views = @View(title = "Table Name"),
            edit = @Edit(title = "Table Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String tableName;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "Parent Class"),
            edit = @Edit(title = "Parent Class", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = SuperModelChoice.class))
    )
    private SuperModel superClass = SuperModel.BASE_MODEL;

    @EruptField(
            views = @View(title = "Package"),
            edit = @Edit(title = "Package", search = @Search(operator = QueryExpression.LIKE),
                    desc = "Written as the package statement of the generated class")
    )
    private String packageName;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Intro"),
            edit = @Edit(title = "Intro", type = EditType.TEXTAREA)
    )
    private String remark;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "class_id")
    @OrderBy("sort")
    @EruptField(
            edit = @Edit(title = "Field Management", type = EditType.TAB_TABLE_ADD)
    )
    private Set<GeneratorField> fields;


}
