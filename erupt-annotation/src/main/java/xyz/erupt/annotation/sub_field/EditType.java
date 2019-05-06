package xyz.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 9/28/18.
 */
public enum EditType {
    INPUT,            //输入框
    TEXTAREA,         //text area
    CHOICE,           //选择框
    SLIDER,           //数字滑块
    DATE,             //日期
    REFERENCE,        //引用
//    CUSTOM_REFER,     //自定义引用类型
    BOOLEAN,          //布尔
    ATTACHMENT,       //附件
    TAB,              //TAB选项卡
    DIVIDE,           //横向分割线
    DEPEND_SWITCH,    //表单依赖开关
    HIDDEN,           //隐藏
    HTML_EDIT,        //富文本编辑器
    JSON_EDIT,         //JSON格式编辑器
    MAP,              //地图
    CODE,             //代码编辑器
    EMPTY,
    STEPS,            //步骤条
}
