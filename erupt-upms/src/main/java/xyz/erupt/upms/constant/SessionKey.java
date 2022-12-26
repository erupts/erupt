package xyz.erupt.upms.constant;

/**
 * @author YuePeng
 * date 2018-12-14.
 */
public class SessionKey {

    private static final String AUTH_SPACE = "erupt-auth:";

    public static final String VERIFY_CODE = AUTH_SPACE + "verify-code:";

    public static final String MENU_VIEW = AUTH_SPACE + "menu-view:";  //菜单列表

    public static final String MENU_VALUE_MAP = AUTH_SPACE + "menu-value-map:"; //菜单类型值作为key的map

    public static final String TOKEN_OLINE = AUTH_SPACE + "token:"; //存储TOKEN使用情况

    public static final String USER_INFO = AUTH_SPACE + "user:"; //缓存用户信息
    //用户相关KEY
    public static final String[] USER_KEY_GROUP = {
            MENU_VIEW,
            MENU_VALUE_MAP,
            TOKEN_OLINE,
            USER_INFO
    };
    public static final String LOGIN_ERROR = AUTH_SPACE + "login-error:"; //登录失败次数

}
