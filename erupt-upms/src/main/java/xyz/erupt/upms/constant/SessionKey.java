package xyz.erupt.upms.constant;

/**
 * @author YuePeng
 * date 2018-12-14.
 */
public class SessionKey {

    private static final String AUTH_SPACE = "eruptAuth:";

    public static final String VERIFY_CODE = AUTH_SPACE + "verifyCode:";


    //菜单类型值作为key的map
    public static final String MENU_VALUE_MAP = AUTH_SPACE + "menu-value-map:";

    //菜单编码作为key的map
    public static final String MENU_CODE_MAP = AUTH_SPACE + "menu-code-map:";

    //菜单列表
    public static final String MENU_VIEW = AUTH_SPACE + "menu-view:";


    //角色权限
    public static final String ROLE_POWER = AUTH_SPACE + "role-power:";

    public static final String USER_TOKEN = AUTH_SPACE + "token:";

    public static final String LOGIN_ERROR = AUTH_SPACE + "login-error:";
}
