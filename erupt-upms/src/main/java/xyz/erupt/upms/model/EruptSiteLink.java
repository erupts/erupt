package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

/**
 * One link of the site settings: help docs, a ticket desk, a support line. It shows in the header
 * toolbar, in the menu under the user's avatar or at the foot of the login page, always as a
 * plain link, never as HTML.
 * Not an entity: the links are a list inside the stored settings document (see
 * {@link EruptSiteSettingStore}), so the list order is the display order and a new attribute needs
 * no DDL. BaseModel only supplies the primary key every erupt model declares (unused here, as in
 * the row-operation forms).
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Erupt(name = "Header Link")
@EruptI18n
@Getter
@Setter
public class EruptSiteLink extends BaseModel {

    // Where a link opens: a new browser window, this window (leaving the app), or a page inside the app frame
    public static final String BLANK = "blank", SELF = "self", FRAME = "frame";

    // Where a link shows: the header toolbar, the menu under the user's avatar, or the foot of the login page
    public static final String HEADER = "header", USER_MENU = "user", LOGIN = "login";

    @EruptField(views = @View(title = "Link Text"), edit = @Edit(title = "Link Text", notNull = true))
    private String name;

    @EruptField(views = @View(title = "Link URL"), edit = @Edit(title = "Link URL", desc = "http(s)://..., a path on this site, mailto: or tel:", notNull = true))
    private String url;

    @EruptField(views = @View(title = "Icon"), edit = @Edit(title = "Icon", type = EditType.ICON))
    private String icon;

    @EruptField(views = @View(title = "Open In"), edit = @Edit(title = "Open In", type = EditType.CHOICE, notNull = true, choiceType = @ChoiceType(vl = {
            @VL(value = BLANK, label = "New Window"),
            @VL(value = SELF, label = "Current Page"),
            @VL(value = FRAME, label = "Inside App")
    })))
    private String openWay = BLANK;

    @EruptField(views = @View(title = "Show In"), edit = @Edit(title = "Show In", type = EditType.CHOICE, notNull = true, choiceType = @ChoiceType(vl = {
            @VL(value = HEADER, label = "Header Toolbar"),
            @VL(value = USER_MENU, label = "Avatar Dropdown"),
            @VL(value = LOGIN, label = "Login Page Footer")
    })))
    private String position = HEADER;

}
