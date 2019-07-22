package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.JavaType;

/**
 * Created by liyuepeng on 9/28/18.
 */
public enum EditType {
    @EditTypeMapping(mapping = "inputType", desc = "输入框 String:vague the like|number:vague the range", allowType = {JavaType.String, JavaType.number})
    INPUT,
    @EditTypeMapping(mapping = "choiceType", desc = "选择框 vague the in", allowType = {JavaType.String, JavaType.number})
    CHOICE,
    @EditTypeMapping(mapping = "sliderType", desc = "数字滑块 vague the range", allowType = {JavaType.number})
    SLIDER,
    @EditTypeMapping(mapping = "dateType", desc = "日期 vague the range", allowType = {JavaType.String, JavaType.date})
    DATE,
    @EditTypeMapping(mapping = "boolType", desc = "布尔", searchVague = false, allowType = {JavaType.bool})
    BOOLEAN,
    @EditTypeMapping(mapping = "attachmentType", desc = "附件", search = false, allowType = {JavaType.String}, excelOperator = false)
    ATTACHMENT,
    @EditTypeMapping(mapping = "dependSwitchType", desc = "表单依赖开关", searchVague = false, allowType = {JavaType.number})
    DEPEND_SWITCH,
    @EditTypeMapping(desc = "横向分割线与描述", search = false, allowType = {JavaType.not_know}, excelOperator = false)
    DIVIDE,
    @EditTypeMapping(desc = "隐藏", search = false, allowType = {JavaType.not_know}, excelOperator = false)
    HIDDEN,
    @EditTypeMapping(desc = "富文本编辑器", allowType = {JavaType.String})
    HTML_EDIT,
    @EditTypeMapping(desc = "大文本域", allowType = {JavaType.String})
    TEXTAREA,
    @EditTypeMapping(mapping = "combineType", desc = "表格合并", searchVague = false, allowType = {JavaType.object})
    COMBINE,
    @EditTypeMapping(mapping = "referenceTreeType", desc = "树引用", searchVague = false, allowType = {JavaType.object})
    REFERENCE_TREE,
    @EditTypeMapping(mapping = "referenceTableType", desc = "表格引用", searchVague = false, allowType = {JavaType.bool})
    REFERENCE_TABLE,
    @EditTypeMapping(desc = "树引用", allowType = {JavaType.object}, search = false, excelOperator = false)
    TAB_TREE,
    @EditTypeMapping(desc = "表格引用", allowType = {JavaType.object}, search = false, excelOperator = false)
    TAB_TABLE_ADD,
    @EditTypeMapping(desc = "表格引用", allowType = {JavaType.object}, search = false, excelOperator = false)
    TAB_TABLE_REFER,

    JSON_EDIT,         //JSON格式编辑器
    MAP,              //地图
    CODE,             //代码编辑器
    EMPTY,
    STEPS,            //步骤条
    CUSTOM_REFER,     //自定义引用类型
}
