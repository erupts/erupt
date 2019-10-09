package xyz.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 11/12/18.
 */
public enum ViewType {
    AUTO, //自动
    TEXT, //普通文字
    IMAGE,//图片
    SWF,//flash文件
    HTML,//HTML
    QR_CODE,//二维码

//    HTTP_DIALOG,
//    HTTP_ATTACHMENT,

    LINK,//链接
    LINK_DIALOG,//对话框方式打开连接

    DOWNLOAD,//下载
    ATTACHMENT,//在新标签页中查看，不同于下载，特殊mine类型可以在网页中直接预览，如：pdf,mp4,svg,png等
    ATTACHMENT_DIALOG, //对话框方式展示打开附件
}
