package xyz.erupt.share.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The session a visitor gets for a link, and what it opens: for a form the erupt, for a page the
 * menu's type and value, which the frontend turns into the route it uses for that menu type.
 *
 * @author YuePeng
 */
@Getter
@AllArgsConstructor
public class ShareSessionVo {

    private String token;

    private String type;

    // FORM: the erupt name; MENU: the menu value
    private String target;

    // MENU: the menu type (tpl, bi, cube, aiCanvas, table...); null for a form
    private String menuType;

    // Minutes until the session ends
    private int expireMinutes;
}
