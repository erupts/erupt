package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.fun.AutoCompleteHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import java.beans.Transient;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public @interface Edit {

    String title();

    @Comment("描述")
    String desc() default "";

    @Comment("是否必填")
    boolean notNull() default false;

    @Comment("是否显示")
    boolean show() default true;

    @Comment("是否只读")
    boolean readOnly() default false;

    @Comment("输入字段预期值的提示信息")
    String placeHolder() default "";

    @Match("value.dependField()")
    @Comment("显示依赖")
    ShowBy showBy() default @ShowBy(dependField = "", expr = "");

    @Comment("查询项")
    Search search() default @Search(false);

    @Transient
    @Comment("排序表达式，在修饰类型为对象时可用")
    String orderBy() default "";

    @Transient
    @Comment("数据过滤表达式，在修饰类型为对象时可用")
    Filter[] filter() default {};

    @Comment("组件类型")
    EditType type() default EditType.AUTO;

    @Match("item.type()=='INPUT'")
    InputType inputType() default @InputType;

    @Match("item.type()=='NUMBER'")
    NumberType numberType() default @NumberType;

    @Match("item.type()=='SLIDER'")
    SliderType sliderType() default @SliderType(max = 999);

    @Match("item.type()=='DATE'")
    DateType dateType() default @DateType;

    @Match("item.type()=='BOOLEAN'")
    BoolType boolType() default @BoolType;

    @Match("item.type()=='CHOICE'")
    ChoiceType choiceType() default @ChoiceType;

    @Match("item.type()=='TAGS'")
    TagsType tagsType() default @TagsType;

    @Match("item.type()=='ATTACHMENT'")
    AttachmentType attachmentType() default @AttachmentType;

    @Match("item.type()=='HTML_EDITOR'")
    HtmlEditorType htmlEditorType() default @HtmlEditorType(HtmlEditorType.Type.UEDITOR);

    @Match("item.type()=='AUTO_COMPLETE'")
    AutoCompleteType autoCompleteType() default @AutoCompleteType(handler = AutoCompleteHandler.class);

    @Match("item.type()=='REFERENCE_TREE'")
    ReferenceTreeType referenceTreeType() default @ReferenceTreeType;

    @Match("item.type()=='REFERENCE_TABLE'")
    ReferenceTableType referenceTableType() default @ReferenceTableType;

    @Match("item.type()=='CHECKBOX'")
    CheckboxType checkboxType() default @CheckboxType;

    @Match("item.type()=='CODE_EDITOR'")
    CodeEditorType codeEditType() default @CodeEditorType(language = "sql");

    @Transient
    Tpl tplType() default @Tpl(path = "");

}