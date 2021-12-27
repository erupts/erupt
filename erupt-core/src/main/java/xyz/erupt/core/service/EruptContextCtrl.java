package xyz.erupt.core.service;

import xyz.erupt.core.view.EruptContext;

/**
 * @author YuePeng
 * date 2021/12/27 00:00
 */
public class EruptContextCtrl {

    private static final ThreadLocal<EruptContext> threadLocal = new ThreadLocal<>();

    public static EruptContext get() {
        return threadLocal.get();
    }

    public static void set(EruptContext name) {
        threadLocal.set(name);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
