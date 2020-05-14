package xyz.erupt.auth.constant;

/**
 * @author liyuepeng
 * @date 2018-12-14.
 */
public class SessionKey {

    private static final String AUTH_SPACE = "eruptAuth:";

    public static final String VERIFY_CODE = AUTH_SPACE + "verifyCode:";

    public static final String USER = AUTH_SPACE + "user:";

    public static final String MENU_TREE = AUTH_SPACE + "menu:";

    public static final String MENU_LIST = AUTH_SPACE + "menuList:";

    public static final String USER_TOKEN = AUTH_SPACE + "token:";

    public static final String LOGIN_ERROR = AUTH_SPACE + "login-error:";
}
