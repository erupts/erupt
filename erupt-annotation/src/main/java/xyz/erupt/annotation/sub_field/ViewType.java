package xyz.erupt.annotation.sub_field;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2018-11-12.
 */
public enum ViewType {
    @Comment("Automatically determined based on return value type and other characteristics")
    AUTO,
    @Comment("Plain text")
    TEXT,
    @Comment("Color")
    COLOR,
    @Comment("Safe text rendering")
    SAFE_TEXT,
    @Comment("Image")
    IMAGE,
    @Comment("Image in BASE64 format")
    IMAGE_BASE64,
    @Comment("Flash file")
    SWF,
    @Comment("HTML")
    HTML,
    @Comment("Mobile-style display")
    MOBILE_HTML,
    @Comment("QR code")
    QR_CODE,
    @Comment("Link")
    LINK,
    @Comment("Open link in a dialog")
    LINK_DIALOG,
    @Comment("Download")
    DOWNLOAD,
    @Comment("View in a new tab; unlike download, special MIME types can be previewed directly in the browser, e.g. pdf, mp4, svg, png")
    ATTACHMENT,
    @Comment("Display attachment in a dialog")
    ATTACHMENT_DIALOG,
    @Comment("Date")
    DATE,
    @Comment("Date and time")
    DATE_TIME,
    @Comment("Boolean switch")
    BOOLEAN,
    @Comment("Numeric value")
    NUMBER,
    @Comment("Progress bar; when edit type is SLIDER the max value comes from SliderType.max, otherwise 100")
    PROGRESS,
    @Comment("Map")
    MAP,
    @Comment("Code")
    CODE,
    @Comment("Display one-to-many or many-to-many data")
    TAB_VIEW,
    @Comment("Markdown editor")
    MARKDOWN,
    @Comment("Password mask, the stored value is replaced with a placeholder and never sent to the client")
    PASSWORD,
}
