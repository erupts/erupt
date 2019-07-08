package xyz.erupt.auth.constant;

/**
 * Created by liyuepeng on 2018-12-14.
 */
public class RedisKey {

    private static final String LIMIT_SPACE = "limit:";

    public static final String VERIFY_CODE = LIMIT_SPACE + "verifyCode:";

    public static final String MENU_TREE = LIMIT_SPACE + "menu:";

    public static final String MENU_LIST = LIMIT_SPACE + "menuList:";

    public static final String USER_TOKEN = LIMIT_SPACE + "token:";
}
