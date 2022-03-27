package xyz.erupt.upms.constant;

/**
 * @author YuePeng
 * date 2018-12-14.
 */
public class SessionKey {

    private static final String AUTH_SPACE = "eruptAuth:";

    public static final String VERIFY_CODE = AUTH_SPACE + "verifyCode:";

    public static final String MENU_VIEW = AUTH_SPACE + "menu-view:";  //菜单列表

    public static final String MENU_VALUE_MAP = AUTH_SPACE + "menu-value-map:"; //菜单类型值作为key的map

    public static final String USER_TOKEN = AUTH_SPACE + "token:";

    public static final String LOGIN_ERROR = AUTH_SPACE + "login-error:";
}
