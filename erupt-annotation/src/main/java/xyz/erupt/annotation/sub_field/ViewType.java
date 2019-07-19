package xyz.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 11/12/18.
 */
public enum ViewType {
    TEXT, //普通文字
    IMAGE,//图片
    SWF,//flash文件
    HTML,//HTML
    QR_CODE,//二维码

    LINK,//链接
    LINK_DIALOG,//对话框方式打开连接

    DOWNLOAD,//下载
    ATTACHMENT,//在新标签页中查看，不同于下载，特殊文件可以在网页中预览，如：video,png等
    ATTACHMENT_DIALOG, //对话框方式展示打开附件
}
