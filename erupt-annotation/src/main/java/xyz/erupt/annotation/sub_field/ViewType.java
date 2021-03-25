package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-11-12.
 */
public enum ViewType {
    @Comment("根据返回值等特征自动判断")
    AUTO,
    @Comment("普通文字")
    TEXT,
    @Comment("图片")
    IMAGE,
    @Comment("图片BASE64")
    IMAGE_BASE64,
    @Comment("flash文件")
    SWF,
    @Comment("HTML")
    HTML,
    @Comment("手机端方式展示")
    MOBILE_HTML,
    @Comment("二维码")
    QR_CODE,
    @Comment("链接")
    LINK,
    @Comment("对话框方式打开链接")
    LINK_DIALOG,
    @Comment("下载")
    DOWNLOAD,
    @Comment("在新标签页中查看，不同于下载，特殊mine类型可以在网页中直接预览，如：pdf,mp4,svg,png等")
    ATTACHMENT,
    @Comment("对话框方式展示附件")
    ATTACHMENT_DIALOG,
    @Comment("时间")
    DATE,
    @Comment("开关")
    BOOLEAN,
    @Comment("数值")
    NUMBER,
    @Comment("地图")
    MAP,
    @Comment("代码")
    CODE,
    @Comment("显示一对多,对多对数据")
    TAB_VIEW,
    @Deprecated
    @Comment("markdown 编辑器")
    MARKDOWN,
}
