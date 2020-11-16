package xyz.erupt.annotation.sub_field;

/**
 * @author liyuepeng
 * @date 2018-11-12.
 */
public enum ViewType {
    //自动
    AUTO,
    //普通文字
    TEXT,
    //图片
    IMAGE,
    IMAGE_BASE64,
    //flash文件
    SWF,
    //HTML
    HTML,
    //手机端方式展示
    MOBILE_HTML,
    //二维码
    QR_CODE,
    //链接
    LINK,
    //对话框方式打开链接
    LINK_DIALOG,
    //下载
    DOWNLOAD,
    //在新标签页中查看，不同于下载，特殊mine类型可以在网页中直接预览，如：pdf,mp4,svg,png等
    ATTACHMENT,
    //对话框方式展示打开附件
    ATTACHMENT_DIALOG,
    DATE,
    BOOLEAN,
    NUMBER,
    MAP,
    CODE,
}
