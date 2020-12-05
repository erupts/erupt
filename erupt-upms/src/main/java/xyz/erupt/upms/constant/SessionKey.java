package xyz.erupt.upms.constant;

/**
 * @author liyuepeng
 * @date 2018-12-14.
 */
public class SessionKey {

    private static final String AUTH_SPACE = "eruptAuth:";

    public static final String VERIFY_CODE = AUTH_SPACE + "verifyCode:";

    public static final String MENU = AUTH_SPACE + "menu:";

    public static final String MENU_VIEW = AUTH_SPACE + "menu-view:";

    public static final String USER_TOKEN = AUTH_SPACE + "token:";

    public static final String LOGIN_ERROR = AUTH_SPACE + "login-error:";
}
