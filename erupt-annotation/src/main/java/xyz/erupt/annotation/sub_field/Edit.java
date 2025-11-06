package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.AutoCompleteHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Edit {

    String title();

    @Comment("描述")
    String desc() default "";

    @Comment("是否必填")
    boolean notNull() default false;

    @Comment("是否显示")
    boolean show() default true;

    @Transient
    @Comment("动态渲染配置")
    ExprBool ifRender() default @ExprBool;

    @Comment("是否只读")
    @EruptProperty(alias = "readOnly")
    Readonly readonly() default @Readonly(add = false, edit = false);

    @Comment("表单提示信息")
    String placeHolder() default "";

    @Match("#value.condition() != ''")
    @Comment("表单动态处理")
    Dynamic dynamic() default @Dynamic(dependField = "", condition = "");

    @Comment("查询项")
    Search search() default @Search(false);

    @Transient
    @Comment("排序表达式，在修饰类型为 ORM 对象时可用")
    String orderBy() default "";

    @Transient
    @Comment("数据过滤表达式，在修饰类型为对象时可用")
    Filter[] filter() default {};

    @Comment("组件类型")
    EditType type() default EditType.AUTO;

    @Match("#item.type().toString()=='INPUT'")
    InputType inputType() default @InputType;

    @Match("#item.type().toString()=='NUMBER'")
    NumberType numberType() default @NumberType;

    @Match("#item.type().toString()=='SLIDER'")
    SliderType sliderType() default @SliderType(max = 999);

    @Match("#item.type().toString()=='RATE'")
    RateType rateType() default @RateType;

    @Match("#item.type().toString()=='DATE'")
    DateType dateType() default @DateType;

    @Match("#item.type().toString()=='BOOLEAN'")
    BoolType boolType() default @BoolType;

    @Match("#item.type().toString()=='CHOICE'")
    ChoiceType choiceType() default @ChoiceType;

    @Match("#item.type().toString()=='MULTI_CHOICE'")
    MultiChoiceType multiChoiceType() default @MultiChoiceType;

    @Match("#item.type().toString()=='TAGS'")
    TagsType tagsType() default @TagsType;

    @Match("#item.type().toString()=='ATTACHMENT'")
    AttachmentType attachmentType() default @AttachmentType;

    @Match("#item.type().toString()=='HTML_EDITOR'")
    HtmlEditorType htmlEditorType() default @HtmlEditorType(HtmlEditorType.Type.UEDITOR);

    @Match("#item.type().toString()=='AUTO_COMPLETE'")
    AutoCompleteType autoCompleteType() default @AutoCompleteType(handler = AutoCompleteHandler.class);

    @Match("#item.type().toString()=='REFERENCE_TREE'")
    ReferenceTreeType referenceTreeType() default @ReferenceTreeType;

    @Match("#item.type().toString()=='REFERENCE_TABLE' || #item.type().toString()=='TAB_TABLE_REFER'")
    ReferenceTableType referenceTableType() default @ReferenceTableType;

    @Transient
    CheckboxType checkboxType() default @CheckboxType;

    @Match("#item.type().toString()=='CODE_EDITOR'")
    CodeEditorType codeEditType() default @CodeEditorType(language = "text");

    @Transient
    Tpl tplType() default @Tpl(path = "", enable = false);

}