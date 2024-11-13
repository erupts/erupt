package xyz.erupt.core.invoke;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 2024/6/29 15:38
 */
public class DataProxyContext {

    private static final ThreadLocal<Data> PRE_DATA_PROXY_THREADLOCAL = new ThreadLocal<>();

    public static String[] params() {
        return get().getParams();
    }

    public static Class<?> currentClass() {
        return get().getEruptModel().getClazz();
    }

    public static Data get() {
        return PRE_DATA_PROXY_THREADLOCAL.get();
    }

    static void set(Data data) {
        PRE_DATA_PROXY_THREADLOCAL.set(data);
    }

    static void remove() {
        PRE_DATA_PROXY_THREADLOCAL.remove();
    }

    @Getter
    @Setter
    public static class Data {

        private EruptModel eruptModel;

        private String[] params;

        public Data(EruptModel eruptModel, String[] params) {
            this.eruptModel = eruptModel;
            this.params = params;
        }

        public Data() {
        }
    }

}
