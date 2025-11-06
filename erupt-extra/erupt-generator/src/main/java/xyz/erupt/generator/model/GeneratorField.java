package xyz.erupt.generator.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.generator.base.GeneratorType;
import xyz.erupt.jpa.model.BaseModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EruptI18n
@Erupt(name = "Erupt字段信息")
@Table(name = "e_generator_field")
@Entity
@Getter
@Setter
public class GeneratorField extends BaseModel implements ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "字段名"),
            edit = @Edit(title = "字段名", notNull = true,
                    desc = "驼峰命名法，字母以小写开头，其后每个单词首字母大写")
    )
    private String fieldName;

    @EruptField(
            views = @View(title = "显示名称"),
            edit = @Edit(title = "显示名称", notNull = true)
    )
    private String showName;

    @EruptField(
            views = @View(title = "显示顺序", sortable = true),
            edit = @Edit(title = "显示顺序", notNull = true)
    )
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(100)")
    @EruptField(
            views = @View(title = "编辑类型"),
            edit = @Edit(title = "编辑类型",
                    notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(type = ChoiceType.Type.RADIO, fetchHandler = GeneratorField.class))
    )
    private GeneratorType type = GeneratorType.INPUT;

    @EruptField(
            views = @View(title = "关联实体类"),
            edit = @Edit(title = "关联实体类", dynamic = @Dynamic(dependField = "type",
                    match = Dynamic.Ctrl.NOTNULL,
                    condition = "value.indexOf('REFERENCE') != -1 || value.indexOf('TAB') != -1 || value == 'CHECKBOX' || value == 'COMBINE'"))
    )
    private String linkClass;

    @EruptField(
            views = @View(title = "查询项"),
            edit = @Edit(title = "查询项", notNull = true)
    )
    private Boolean query = true;

    @EruptField(
            views = @View(title = "字段排序"),
            edit = @Edit(title = "字段排序", notNull = true)
    )
    private Boolean sortable = false;

    @EruptField(
            views = @View(title = "是否必填"),
            edit = @Edit(title = "是否必填", notNull = true)
    )
    private Boolean notNull = true;

    @EruptField(
            views = @View(title = "是否显示"),
            edit = @Edit(title = "是否显示", notNull = true)
    )
    private Boolean isShow = true;


    @Override
    public List<VLModel> fetch(String[] params) {
        return Arrays.stream(GeneratorType.values()).map(it -> new VLModel(it.name(), it.getName(), it.name())).collect(Collectors.toList());
    }

}
