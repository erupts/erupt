package xyz.erupt.core.context;

/**
 * Carries a JSON snapshot of the entity's state before an UPDATE,
 * so that the security layer can produce a before/after diff in operation logs.
 *
 * @author YuePeng
 */
public class OldEntityTL {

    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void set(String json) {
        threadLocal.set(json);
    }

    public static String get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
