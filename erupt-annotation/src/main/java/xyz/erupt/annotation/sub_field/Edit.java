package xyz.erupt.annotation.sub_field;

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

    String desc() default "";

    boolean notNull() default false;

    boolean show() default true;

    @Match("value.dependField()")
    ShowBy showBy() default @ShowBy(dependField = "", expr = "");

    boolean readOnly() default false;

    String placeHolder() default "";

    Search search() default @Search(false);

    @Transient
    String orderBy() default "";

    @Transient
    Filter[] filter() default {};

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

    @Match("item.type()=='CODE_EDITOR'")
    CodeEditorType codeEditType() default @CodeEditorType(language = "sql");

    @Match("item.type()=='MAP'")
    MapType mapType() default @MapType;

    @Transient
    Tpl tplType() default @Tpl(path = "");

}