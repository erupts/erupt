package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.JavaTypeEnum;
import xyz.erupt.annotation.sub_field.sub_edit.*;

/**
 * @author liyuepeng
 * @date 2018-09-28.
 */
public enum EditType {
    @EditTypeMapping(desc = "框架自动检测类型", allowType = {JavaTypeEnum.any})
    AUTO,
    @EditTypeMapping(mapping = InputType.class, desc = "文本输入", search = @Search(vague = true), allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    INPUT,
    @EditTypeMapping(mapping = NumberType.class, desc = "数字输入", search = @Search(vague = true), allowType = {JavaTypeEnum.number})
    NUMBER,
    @EditTypeMapping(mapping = SliderType.class, desc = "数字滑块", search = @Search(vague = true), allowType = {JavaTypeEnum.number})
    SLIDER,
    @EditTypeMapping(mapping = DateType.class, desc = "日期", search = @Search(vague = true), allowType = {JavaTypeEnum.String, JavaTypeEnum.date})
    DATE,
    @EditTypeMapping(mapping = BoolType.class, desc = "布尔", allowType = {JavaTypeEnum.bool})
    BOOLEAN,
    @EditTypeMapping(mapping = ChoiceType.class, desc = "选择框", search = @Search(vague = true), allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    CHOICE,
    @EditTypeMapping(mapping = TagsType.class, desc = "标签选择器", allowType = {JavaTypeEnum.String, JavaTypeEnum.number})
    TAGS,
    @EditTypeMapping(mapping = AttachmentType.class, desc = "附件", search = @Search(value = false), allowType = {JavaTypeEnum.String}, excelOperator = false)
    ATTACHMENT,
    @EditTypeMapping(mapping = AutoCompleteType.class, desc = "自动联想", allowType = {JavaTypeEnum.String})
    AUTO_COMPLETE,
    @EditTypeMapping(mapping = HtmlEditorType.class, desc = "富文本编辑器", allowType = {JavaTypeEnum.String})
    HTML_EDITOR,
    @EditTypeMapping(desc = "多行文本框", allowType = {JavaTypeEnum.String}, nameInfer = {"desc", "remark"})
    TEXTAREA,
    @EditTypeMapping(desc = "地图", allowType = {JavaTypeEnum.String}, search = @Search(value = false), excelOperator = false)
    MAP,
    @EditTypeMapping(desc = "代码编辑器", allowType = {JavaTypeEnum.String})
    CODE_EDITOR,


    @EditTypeMapping(mapping = ReferenceTreeType.class, desc = "树引用（多对一)", search = @Search(value = false), allowType = {JavaTypeEnum.object})
    REFERENCE_TREE,
    @EditTypeMapping(mapping = ReferenceTableType.class, desc = "表格引用（多对一)", search = @Search(value = false), allowType = {JavaTypeEnum.bool})
    REFERENCE_TABLE,


    @EditTypeMapping(mapping = CheckboxType.class, desc = "多选（多对多）", allowType = {JavaTypeEnum.object}, search = @Search(value = false), excelOperator = false)
    CHECKBOX,
    @EditTypeMapping(desc = "树引用（多对多）", allowType = {JavaTypeEnum.object}, search = @Search(value = false), excelOperator = false)
    TAB_TREE,
    @EditTypeMapping(desc = "表格引用（多对多）", allowType = {JavaTypeEnum.object}, search = @Search(value = false), excelOperator = false)
    TAB_TABLE_REFER,
    @EditTypeMapping(desc = "表格添加（一对多)", allowType = {JavaTypeEnum.object}, search = @Search(value = false), excelOperator = false)
    TAB_TABLE_ADD,
    @EditTypeMapping(desc = "表格合并（一对一）", search = @Search(value = false), allowType = {JavaTypeEnum.object})
    COMBINE,


    @EditTypeMapping(desc = "自定义HTML模板", allowType = {JavaTypeEnum.String}, search = @Search(value = false), excelOperator = false)
    TPL,
    @EditTypeMapping(desc = "横向分割线与描述", search = @Search(value = false), allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    DIVIDE,
    @EditTypeMapping(desc = "隐藏", search = @Search(value = false), allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    HIDDEN,
    @EditTypeMapping(desc = "空（仍占据组件位置）", search = @Search(value = false), allowType = {JavaTypeEnum.not_know}, excelOperator = false)
    EMPTY,
}
