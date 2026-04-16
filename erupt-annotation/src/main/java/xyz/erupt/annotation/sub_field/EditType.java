package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.JavaTypeEnum;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_field.sub_edit.*;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public enum EditType {
    @EditTypeMapping(desc = "Auto-detected type by framework", allowType = {JavaTypeEnum.any})
    AUTO,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = InputType.class, desc = "Text input", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    INPUT,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = NumberType.class, desc = "Number input", allowType = {JavaTypeEnum.number})
    NUMBER,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = SliderType.class, desc = "Number slider", allowType = {JavaTypeEnum.number})
    SLIDER,
    @EditTypeMapping(desc = "Color picker", allowType = {JavaTypeEnum.String})
    COLOR,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = RateType.class, desc = "Rating", allowType = {JavaTypeEnum.number})
    RATE,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = DateType.class, desc = "Date", allowType = {JavaTypeEnum.String, JavaTypeEnum.date})
    DATE,
    @EditTypeSearch
    @EditTypeMapping(mapping = BoolType.class, desc = "Boolean", allowType = {JavaTypeEnum.bool})
    BOOLEAN,
    @EditTypeSearch(vagueMethod = QueryExpression.IN)
    @EditTypeMapping(mapping = ChoiceType.class, desc = "Single select", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    CHOICE,
    @EditTypeMapping(mapping = MultiChoiceType.class, desc = "Multi select", allowType = {JavaTypeEnum.object}, excelOperator = false)
    MULTI_CHOICE,
    @EditTypeSearch
    @EditTypeMapping(mapping = TagsType.class, desc = "Tags selector", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    TAGS,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = AutoCompleteType.class, desc = "Auto complete", allowType = {JavaTypeEnum.String})
    AUTO_COMPLETE,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(desc = "Multi-line text area", allowType = {JavaTypeEnum.String}, nameInfer = {"remark"})
    TEXTAREA,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = HtmlEditorType.class, desc = "Rich text editor", allowType = {JavaTypeEnum.String})
    HTML_EDITOR,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = CodeEditorType.class, desc = "Code editor", allowType = {JavaTypeEnum.String})
    CODE_EDITOR,
    @EditTypeMapping(desc = "MarkDown editor", allowType = {JavaTypeEnum.String})
    MARKDOWN,
    @EditTypeMapping(mapping = AttachmentType.class, desc = "Attachment upload", allowType = {JavaTypeEnum.String}, excelOperator = false)
    ATTACHMENT,
    @EditTypeMapping(desc = "Map", allowType = {JavaTypeEnum.String}, excelOperator = false)
    MAP,
    @EditTypeMapping(desc = "Custom HTML template", allowType = {JavaTypeEnum.String}, excelOperator = false)
    TPL,
    @EditTypeMapping(desc = "Horizontal divider with description", allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    DIVIDE,
    @EditTypeMapping(desc = "Hidden", allowType = {JavaTypeEnum.any}, excelOperator = false)
    HIDDEN,
    @EditTypeMapping(desc = "Empty (still occupies component space)", allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    EMPTY,
    @EditTypeMapping(desc = "Signature pad", allowType = {JavaTypeEnum.String}, excelOperator = false)
    SIGNATURE,

    @Comment("==================================")
    @Comment("The following components are used on complex object fields")
    @Comment("==================================")

    @EditTypeSearch
    @EditTypeMapping(mapping = ReferenceTreeType.class, desc = "Tree reference (many-to-one)", allowType = {JavaTypeEnum.object})
    REFERENCE_TREE,
    @EditTypeSearch
    @EditTypeMapping(mapping = ReferenceTableType.class, desc = "Table reference (many-to-one)", allowType = {JavaTypeEnum.object})
    REFERENCE_TABLE,
    @EditTypeMapping(mapping = CheckboxType.class, desc = "Multi-select (many-to-many)", allowType = {JavaTypeEnum.object}, excelOperator = false)
    CHECKBOX,
    @EditTypeMapping(desc = "Multi-select tree (many-to-many)", allowType = {JavaTypeEnum.object}, excelOperator = false)
    TAB_TREE,
    @EditTypeMapping(desc = "Multi-select table (many-to-many)", allowType = {JavaTypeEnum.object}, excelOperator = false)
    TAB_TABLE_REFER,
    @EditTypeMapping(desc = "Table add (one-to-many)", allowType = {JavaTypeEnum.object}, excelOperator = false)
    TAB_TABLE_ADD,
    @EditTypeMapping(desc = "Table merge (one-to-one)", allowType = {JavaTypeEnum.object})
    COMBINE,
}
