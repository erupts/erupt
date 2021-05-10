package xyz.erupt.bi.constant;

/**
 * @author YuePeng
 * date 2021/2/24 23:21
 */
public class ScriptPlaceholderConst {

    private static final String SYMBOL = "{}";

    private static final String PLACEHOLDER = "__" + SYMBOL + "__";

    //导出标识符
    public static final String EXPORT_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "export");

    //当前登录用户ID
    public static final String USER_ID_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "uid");

    //request 请求对象
    public static final String REQUEST_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "request");

    //response 响应对象
    public static final String RESPONSE_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "response");

}
