package xyz.erupt.cloud.server.node;

/**
 * @author YuePeng
 * date 2022/3/19 17:05
 */
public class NodeContext {

    private static final ThreadLocal<MetaNode> threadLocal = new ThreadLocal<>();

    public static MetaNode get() {
        return threadLocal.get();
    }

    public static void set(MetaNode metaNode) {
        threadLocal.set(metaNode);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
