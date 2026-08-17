package xyz.erupt.report.constant;

/**
 * @author YuePeng
 * date 2021/2/24 23:21
 */
public class ScriptPlaceholderConst {

    private static final String SYMBOL = "{}";

    private static final String PLACEHOLDER = "__" + SYMBOL + "__";

    // export marker
    public static final String EXPORT_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "export");

    // current logged-in user ID
    public static final String USER_ID_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "uid");

    // HTTP request object
    public static final String REQUEST_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "request");

    // HTTP response object
    public static final String RESPONSE_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "response");

    // current page size
    public static final String PAGE_SIZE_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "pageIndex");

    // current page index
    public static final String PAGE_INDEX_PLACEHOLDER = PLACEHOLDER.replace(SYMBOL, "pageSize");

}
