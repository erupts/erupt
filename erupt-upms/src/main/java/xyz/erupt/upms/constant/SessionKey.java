package xyz.erupt.upms.constant;

/**
 * @author YuePeng
 * date 2018-12-14.
 */
public class SessionKey {

    private static final String AUTH_SPACE = "erupt-auth:";

    public static final String VERIFY_CODE = AUTH_SPACE + "verify-code:";

    public static final String MENU_VIEW = AUTH_SPACE + "menu-view:";  //Menu list

    public static final String MENU_VALUE_MAP = AUTH_SPACE + "menu-value-map:"; //Map with menu type value as key

    public static final String TOKEN_OLINE = AUTH_SPACE + "token:"; //Stores TOKEN usage status

    public static final String USER_INFO = AUTH_SPACE + "user:"; //Cached user information
    //User-related keys
    public static final String[] USER_KEY_GROUP = {
            MENU_VIEW,
            MENU_VALUE_MAP,
            TOKEN_OLINE,
            USER_INFO
    };
    public static final String LOGIN_ERROR = AUTH_SPACE + "login-error:"; //Number of login failures

}
