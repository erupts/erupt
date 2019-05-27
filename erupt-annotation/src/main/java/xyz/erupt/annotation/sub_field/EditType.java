package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.JavaType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

/**
 * Created by liyuepeng on 9/28/18.
 */
public enum EditType {
    @EditTypeMapping(maping = "inputType", desc = "输入框 string:vague the like|number:vague the range", allowType = {JavaType.string, JavaType.number})
    INPUT,
    @EditTypeMapping(maping = "choiceType", desc = "选择框 vague the in", allowType = {JavaType.string, JavaType.number})
    CHOICE,
    @EditTypeMapping(maping = "sliderType", desc = "数字滑块 vague the range", allowType = {JavaType.number})
    SLIDER,
    @EditTypeMapping(maping = "dateType", desc = "日期 vague the range", allowType = {JavaType.string, JavaType.date})
    DATE,
    @EditTypeMapping(maping = "referenceTreeType", desc = "树引用", allowType = {JavaType.object})
    REFERENCE_TREE,
    @EditTypeMapping(maping = "referenceTableType", desc = "表格引用", allowType = {JavaType.bool})
    REFERENCE_TABLE,
    @EditTypeMapping(maping = "boolType", desc = "布尔", allowType = {JavaType.bool})
    BOOLEAN,
    @EditTypeMapping(maping = "attachmentType", search = @Search(false), desc = "附件", allowType = {JavaType.string})
    ATTACHMENT,
    @EditTypeMapping(maping = "tabType", search = @Search(false), desc = "TAB选项卡", allowType = {JavaType.array})
    TAB,
    @EditTypeMapping(maping = "dependSwitchType", desc = "表单依赖开关", allowType = {JavaType.number})
    DEPEND_SWITCH,
    @EditTypeMapping(maping = "", desc = "横向分割线与描述", allowType = {JavaType.not_know})
    DIVIDE,
    @EditTypeMapping(maping = "", desc = "隐藏", allowType = {JavaType.not_know})
    HIDDEN,
    @EditTypeMapping(maping = "", desc = "富文本编辑器", allowType = {JavaType.string})
    HTML_EDIT,


    TEXTAREA,         //text area         vague the like
    JSON_EDIT,         //JSON格式编辑器
    MAP,              //地图
    CODE,             //代码编辑器
    EMPTY,
    STEPS,            //步骤条
    CUSTOM_REFER,     //自定义引用类型
}
