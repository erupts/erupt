package xyz.erupt.config.view;

import com.google.gson.JsonElement;
import org.springframework.web.util.HtmlUtils;
import xyz.erupt.config.boot.ConfigValues;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.annotation.fun.AttachmentProxy;

import java.util.Map;

/**
 * How a stored value looks in a table cell, by type: a swatch for a color, the icon itself, the
 * picture itself for an image, a link for an attachment, tags for a list, the whole text in a box
 * that scrolls past a certain height, the mask for a secret. Everything that came from the user is escaped, except an HTML
 * item, which is shown as the HTML it is, the way erupt's own HTML view does; the markup is ours.
 *
 * @author YuePeng
 */
public final class ConfigValueHtml {

    private static final String BOX = "display:block;max-height:160px;overflow:auto;margin:0;padding:4px 6px;text-align:left;white-space:pre-wrap;word-break:break-all;background:rgba(0,0,0,.03);border-radius:4px;font-size:12px";

    private ConfigValueHtml() {
    }

    public static String render(String type, String value, boolean secret) {
        if (secret || ConfigValues.PASSWORD.equals(type)) {
            return "<i class='fa fa-lock' aria-hidden='true' style='margin-right:4px;opacity:.45'></i><span style='letter-spacing:2px;opacity:.65'>" + ConfigItem.MASK + "</span>";
        }
        if (null == value || value.isEmpty()) return "";
        String text = HtmlUtils.htmlEscape(value);
        switch (null == type ? ConfigValues.STRING : type) {
            case ConfigValues.BOOLEAN -> {
                boolean on = Boolean.parseBoolean(value);
                return "<i class='fa " + (on ? "fa-check-circle' style='color:#52c41a" : "fa-times-circle' style='color:#bfbfbf") + ";margin-right:4px'></i>" + text;
            }
            case ConfigValues.COLOR -> {
                return "<i class='fa fa-square' style='color:" + text + ";font-size:1.2rem;margin-right:6px;vertical-align:middle' aria-hidden='true'></i>" + text;
            }
            case ConfigValues.LIST -> {
                StringBuilder tags = new StringBuilder();
                for (String tag : value.split(",")) {
                    if (!tag.isBlank()) tags.append("<span class='ant-tag' style='margin:1px'>").append(HtmlUtils.htmlEscape(tag.trim())).append("</span>");
                }
                return tags.toString();
            }
            case ConfigValues.IMAGE -> {
                String url = HtmlUtils.htmlEscape(attachmentUrl(value));
                return "<a href='" + url + "' target='_blank'><img src='" + url + "' alt='" + HtmlUtils.htmlEscape(fileName(value))
                        + "' style='max-height:80px;max-width:200px;border-radius:4px;vertical-align:middle'></a>";
            }
            case ConfigValues.ATTACHMENT -> {
                return "<a href='" + HtmlUtils.htmlEscape(attachmentUrl(value)) + "' target='_blank'><i class='fa fa-paperclip' aria-hidden='true' style='margin-right:4px'></i>"
                        + HtmlUtils.htmlEscape(fileName(value)) + "</a>";
            }
            case ConfigValues.ICON -> {
                // a class list, never markup: anything else is shown as text
                return value.matches("[\\w\\s-]+") ? "<i class='" + value + "' style='font-size:1.2rem;margin-right:6px' aria-hidden='true'></i>" + text : text;
            }
            case ConfigValues.HTML -> {
                return "<div style='" + BOX + ";white-space:normal'>" + value + "</div>";
            }
            case ConfigValues.KEY_VALUE -> {
                JsonElement element = ConfigValues.parseJson(value);
                if (null == element || !element.isJsonObject()) return "<pre style='" + BOX + "'>" + text + "</pre>";
                StringBuilder lines = new StringBuilder();
                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                    String item = entry.getValue().isJsonPrimitive() ? entry.getValue().getAsString() : entry.getValue().toString();
                    lines.append("<b>").append(HtmlUtils.htmlEscape(entry.getKey())).append("</b> = ").append(HtmlUtils.htmlEscape(item)).append("\n");
                }
                return "<pre style='" + BOX + "'>" + lines + "</pre>";
            }
            case ConfigValues.TEXT, ConfigValues.JSON, ConfigValues.YAML -> {
                return "<pre style='" + BOX + "'>" + text + "</pre>";
            }
            default -> {
                return text;
            }
        }
    }

    private static String fileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    // The same URL the attachment views open: the configured file domain, else erupt's own handler
    private static String attachmentUrl(String path) {
        AttachmentProxy proxy = EruptUtil.findAttachmentProxy();
        if (null == proxy) return EruptRestPath.ERUPT_ATTACHMENT + path;
        try {
            return EruptUtil.attachmentUrl(proxy, path).toString();
        } catch (RuntimeException e) {
            return path;
        }
    }

}
