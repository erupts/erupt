package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.JavaType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

/**
 * Created by liyuepeng on 9/28/18.
 */
public enum EditType {
    @EditTypeMapping(mapping = "inputType", desc = "输入框 string:vague the like|number:vague the range", allowType = {JavaType.string, JavaType.number})
    INPUT,
    @EditTypeMapping(mapping = "choiceType", desc = "选择框 vague the in", allowType = {JavaType.string, JavaType.number})
    CHOICE,
    @EditTypeMapping(mapping = "sliderType", desc = "数字滑块 vague the range", allowType = {JavaType.number})
    SLIDER,
    @EditTypeMapping(mapping = "dateType", desc = "日期 vague the range", allowType = {JavaType.string, JavaType.date})
    DATE,
    @EditTypeMapping(mapping = "referenceTreeType", desc = "树引用", allowType = {JavaType.object})
    REFERENCE_TREE,
    @EditTypeMapping(mapping = "referenceTableType", desc = "表格引用", allowType = {JavaType.bool})
    REFERENCE_TABLE,
    @EditTypeMapping(mapping = "combineType", desc = "表格合并", search = @Search(false), allowType = {JavaType.object})
    COMBINE,
    @EditTypeMapping(mapping = "boolType", desc = "布尔", allowType = {JavaType.bool})
    BOOLEAN,
    @EditTypeMapping(mapping = "attachmentType", desc = "附件", search = @Search(false), allowType = {JavaType.string}, excelOperator = false)
    ATTACHMENT,
    @EditTypeMapping(mapping = "tabType", desc = "TAB选项卡", search = @Search(false), allowType = {JavaType.array}, excelOperator = false)
    TAB,
    @EditTypeMapping(mapping = "dependSwitchType", desc = "表单依赖开关", allowType = {JavaType.number})
    DEPEND_SWITCH,

    @EditTypeMapping(mapping = "", desc = "横向分割线与描述", allowType = {JavaType.not_know}, excelOperator = false)
    DIVIDE,
    @EditTypeMapping(mapping = "", desc = "隐藏", allowType = {JavaType.not_know}, excelOperator = false)
    HIDDEN,
    @EditTypeMapping(mapping = "", desc = "富文本编辑器", allowType = {JavaType.string})
    HTML_EDIT,

    @EditTypeMapping(mapping = "", desc = "大文本域", allowType = {JavaType.string})
    TEXTAREA,         //text area         vague the like


    JSON_EDIT,         //JSON格式编辑器
    MAP,              //地图
    CODE,             //代码编辑器
    EMPTY,
    STEPS,            //步骤条
    CUSTOM_REFER,     //自定义引用类型
}
