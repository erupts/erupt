package xyz.erupt.security.tl;

import xyz.erupt.security.model.RequestBody;

/**
 * @author YuePeng
 * date 2021/4/6 16:50
 */
public class RequestBodyTL {

    private static final ThreadLocal<RequestBody> threadLocal = new ThreadLocal<>();

    public static RequestBody get() {
        return threadLocal.get();
    }

    public static void set(RequestBody requestBody) {
        threadLocal.set(requestBody);
    }

    public static void remove() {
        threadLocal.remove();
    }
}
