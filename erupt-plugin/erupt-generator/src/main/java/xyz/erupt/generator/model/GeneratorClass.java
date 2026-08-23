package xyz.erupt.generator.model;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.jpa.model.MetaModel;

import java.util.Map;
import java.util.Set;

@EruptI18n
@Erupt(name = "Generate Erupt Code",
        rowOperation = @RowOperation(
                title = "Code Preview", icon = "fa fa-code",
                mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL,
                tpl = @Tpl(path = "generator/erupt-code-skeleton.ftl",
                        engine = Tpl.Engine.FreeMarker,
                        tplHandler = GeneratorClass.class)
        )
)
@Table(name = "e_generator_class")
@Entity
@Getter
@Setter
public class GeneratorClass extends MetaModel implements Tpl.TplHandler {

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

    @SneakyThrows
    @Override
    public void bindTplData(Map<String, Object> binding, String[] params) {
        TemplateHashModel staticModels = BeansWrapper.getDefaultInstance().getStaticModels();
        TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(GeneratorType.class.getName());
        binding.put(GeneratorType.class.getSimpleName(), fileStatics);
    }


}
