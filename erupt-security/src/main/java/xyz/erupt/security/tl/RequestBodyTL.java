package xyz.erupt.security.tl;

import xyz.erupt.security.model.ReqBody;

/**
 * @author YuePeng
 * date 2021/4/6 16:50
 */
public class RequestBodyTL {

    private static final ThreadLocal<ReqBody> threadLocal = new ThreadLocal<>();

    public static ReqBody get() {
        return threadLocal.get();
    }

    public static void set(ReqBody reqBody) {
        threadLocal.set(reqBody);
    }

    public static void remove() {
        threadLocal.remove();
    }
}
