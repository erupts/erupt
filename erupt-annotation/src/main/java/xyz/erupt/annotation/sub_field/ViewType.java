package xyz.erupt.annotation.sub_field;

/**
 * Created by liyuepeng on 11/12/18.
 */
public enum ViewType {
    TEXT, //普通文字
    IMAGE,//图片
    SWF,//flash文件
    LINK,//链接
    //    IFRAME_LINK,
    DOWNLOAD,//下载
    IFRAME, //框架方式展示 如pdf
    ATTACHMENT,//在新标签页中查看，不同于下载，特殊文件可以在网页中预览，如：video,png等
    HTML,//HTML
    QR_CODE,//二维码
}
