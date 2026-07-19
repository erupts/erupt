package xyz.erupt.annotation.sub_field;

import org.intellij.lang.annotations.Language;
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

    @Comment("Description")
    String desc() default "";

    @Comment("AI prompt")
    @Transient
    @Language("markdown")
    String prompt() default "";

    @Comment("Whether the field is required")
    boolean notNull() default false;

    @Comment("Whether to display")
    boolean show() default true;

    @Transient
    @Comment("Accessible by the onchange interface")
    String[] onchangeParams() default {};

    Class<? extends OnChange> onchange() default OnChange.class;

    @Transient
    @Comment("Dynamic rendering configuration")
    ExprBool ifRender() default @ExprBool;

    @Comment("Whether the field is read-only")
    @EruptProperty(alias = "readOnly")
    Readonly readonly() default @Readonly(add = false, edit = false);

    @Comment("Form placeholder hint")
    String placeHolder() default "";

    @Match("#value.condition() != ''")
    @Comment("Dynamic form processing")
    Dynamic dynamic() default @Dynamic(dependField = "", condition = "");

    @Comment("Search configuration")
    Search search() default @Search(false);

    @Transient
    @Comment("Sort expression; applicable when the field type is an ORM entity object")
    @Language(value = "sql", prefix = "select * from t order by")
    String orderBy() default "";

    @Transient
    @Comment("Data filter expression; applicable when the field type is an entity object")
    Filter[] filter() default {};

    @Comment("Component type")
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
    HtmlEditorType htmlEditorType() default @HtmlEditorType(HtmlEditorType.Type.CKEDITOR);

    @Match("#item.type().toString()=='AUTO_COMPLETE'")
    AutoCompleteType autoCompleteType() default @AutoCompleteType(handler = AutoCompleteHandler.class);

    @Match("#item.type().toString()=='REFERENCE_TREE'")
    ReferenceTreeType referenceTreeType() default @ReferenceTreeType;

    @Match("#item.type().toString()=='REFERENCE_TABLE' || #item.type().toString()=='TAB_TABLE_REFER'")
    ReferenceTableType referenceTableType() default @ReferenceTableType;

    @Transient
    CheckboxType checkboxType() default @CheckboxType;

    @Match("#item.type().toString()=='GROUP'")
    GroupType groupType() default @GroupType(fields = {});

    @Match("#item.type().toString()=='CALLOUT'")
    CalloutType calloutType() default @CalloutType;

    @Match("#item.type().toString()=='BUTTON'")
    ButtonType buttonType() default @ButtonType;

    @Match("#item.type().toString()=='CODE_EDITOR'")
    CodeEditorType codeEditType() default @CodeEditorType(language = "text");

    @Transient
    Tpl tplType() default @Tpl(path = "", enable = false);

}