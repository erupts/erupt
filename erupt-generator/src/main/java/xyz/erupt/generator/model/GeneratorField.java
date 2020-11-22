package xyz.erupt.generator.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.auth.model.base.BaseModel;
import xyz.erupt.generator.base.GeneratorType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Erupt(name = "Erupt字段信息")
@Entity
@Table(name = "e_generator_field")
@Getter
@Setter
public class GeneratorField extends BaseModel implements ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "字段名"),
            edit = @Edit(title = "字段名", notNull = true)
    )
    private String fieldName;

    @EruptField(
            views = @View(title = "展示名称"),
            edit = @Edit(title = "展示名称", notNull = true)
    )
    private String showName;

    @EruptField(
            views = @View(title = "组件类型"),
            edit = @Edit(title = "组件类型",
                    notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(type = ChoiceType.Type.SELECT, fetchHandler = GeneratorField.class))
    )
    private String type;

    @EruptField(
            views = @View(title = "关联表"),
            edit = @Edit(title = "关联表")
    )
    private String linkClass;

    @EruptField(
            views = @View(title = "查询列"),
            edit = @Edit(title = "查询列")
    )
    private Boolean query;

    @EruptField(
            views = @View(title = "是否可排序"),
            edit = @Edit(title = "是否可排序")
    )
    private Boolean sortable;

    @EruptField(
            views = @View(title = "是否必填"),
            edit = @Edit(title = "是否必填")
    )
    private Boolean notNull;

    @EruptField(
            views = @View(title = "是否显示"),
            edit = @Edit(title = "是否显示")
    )
    private Boolean isShow;

    @Transient
    private GeneratorType generatorType;

    @Override
    public List<VLModel> fetch(String[] params) {
        List<VLModel> list = new ArrayList<>();
        for (GeneratorType value : GeneratorType.values()) {
            list.add(new VLModel(value.name(), value.getName()));
        }
        return list;
    }


}
