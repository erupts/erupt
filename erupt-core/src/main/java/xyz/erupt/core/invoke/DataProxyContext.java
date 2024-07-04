package xyz.erupt.core.invoke;

/**
 * @author YuePeng
 * date 2024/6/29 15:38
 */
public class DataProxyContext {

    private static final ThreadLocal<String[]> PRE_DATA_PROXY_THREADLOCAL = new ThreadLocal<>();

    public static String[] params() {
        return PRE_DATA_PROXY_THREADLOCAL.get();
    }

    static void set(String[] params) {
        PRE_DATA_PROXY_THREADLOCAL.set(params);
    }

    static void remove() {
        PRE_DATA_PROXY_THREADLOCAL.remove();
    }

}
