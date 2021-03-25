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
    @EditTypeMapping(desc = "框架自动检测类型", allowType = {JavaTypeEnum.any})
    AUTO,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = InputType.class, desc = "文本输入", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    INPUT,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = NumberType.class, desc = "数字输入", allowType = {JavaTypeEnum.number})
    NUMBER,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = SliderType.class, desc = "数字滑块", allowType = {JavaTypeEnum.number})
    SLIDER,
    @EditTypeSearch(vagueMethod = QueryExpression.RANGE)
    @EditTypeMapping(mapping = DateType.class, desc = "日期", allowType = {JavaTypeEnum.String, JavaTypeEnum.date})
    DATE,
    @EditTypeSearch
    @EditTypeMapping(mapping = BoolType.class, desc = "布尔", allowType = {JavaTypeEnum.bool})
    BOOLEAN,
    @EditTypeSearch(vagueMethod = QueryExpression.IN)
    @EditTypeMapping(mapping = ChoiceType.class, desc = "选择框", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    CHOICE,
    @EditTypeSearch
    @EditTypeMapping(mapping = TagsType.class, desc = "标签选择器", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    TAGS,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = AutoCompleteType.class, desc = "自动完成", allowType = {JavaTypeEnum.String})
    AUTO_COMPLETE,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(desc = "多行文本框", allowType = {JavaTypeEnum.String}, nameInfer = {"desc", "remark"})
    TEXTAREA,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = HtmlEditorType.class, desc = "富文本编辑器", allowType = {JavaTypeEnum.String})
    HTML_EDITOR,
    @EditTypeSearch(vagueMethod = QueryExpression.LIKE)
    @EditTypeMapping(mapping = CodeEditorType.class, desc = "代码编辑器", allowType = {JavaTypeEnum.String})
    CODE_EDITOR,
    @Deprecated
    @EditTypeMapping(desc = "MarkDown编辑器", allowType = {JavaTypeEnum.String})
    MARKDOWN,
    @EditTypeMapping(mapping = AttachmentType.class, desc = "附件上传", allowType = {JavaTypeEnum.String}, excelOperator = false)
    ATTACHMENT,
    @EditTypeMapping(desc = "地图", allowType = {JavaTypeEnum.String}, excelOperator = false)
    MAP,
    @EditTypeMapping(desc = "自定义HTML模板", allowType = {JavaTypeEnum.String}, excelOperator = false)
    TPL,
    @EditTypeMapping(desc = "横向分割线与描述", allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    DIVIDE,
    @EditTypeMapping(desc = "隐藏", allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    HIDDEN,
    @EditTypeMapping(desc = "空（仍占据组件位置）", allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    EMPTY,

    @Comment("==================================")
    @Comment("以下对象使用较为复杂，作用于复杂对象基础上")
    @EditTypeSearch
    @EditTypeMapping(mapping = ReferenceTreeType.class, desc = "树引用（多对一)", allowType = {JavaTypeEnum.object})
    REFERENCE_TREE,
    @EditTypeSearch
    @EditTypeMapping(mapping = ReferenceTableType.class, desc = "表格引用（多对一)", allowType = {JavaTypeEnum.bool})
    REFERENCE_TABLE,
    @EditTypeMapping(mapping = CheckboxType.class, desc = "多选（多对多）", allowType = {JavaTypeEnum.object}, excelOperator = false)
    CHECKBOX,
    @EditTypeMapping(desc = "多选树（多对多）", allowType = {JavaTypeEnum.object}, excelOperator = false)
    TAB_TREE,
    @EditTypeMapping(desc = "多选表格（多对多）", allowType = {JavaTypeEnum.object}, excelOperator = false)
    TAB_TABLE_REFER,
    @EditTypeMapping(desc = "表格添加（一对多)", allowType = {JavaTypeEnum.object}, excelOperator = false)
    TAB_TABLE_ADD,
    @EditTypeMapping(desc = "表格合并（一对一）", allowType = {JavaTypeEnum.object})
    COMBINE,
}
