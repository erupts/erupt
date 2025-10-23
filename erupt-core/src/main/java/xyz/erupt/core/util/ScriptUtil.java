package xyz.erupt.core.util;

import lombok.SneakyThrows;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;

import java.util.Map;

public class ScriptUtil {

    private static final Context.Builder builder;

    public static final String LANGUAGE_ID = "js";

    static {
        builder = Context.newBuilder(LANGUAGE_ID)
                .option("js.ecmascript-version", "2022")
                .allowHostAccess(HostAccess.NONE)
                .allowPolyglotAccess(PolyglotAccess.NONE);
    }

    @SneakyThrows
    public static Value eval(String script, Map<String, Object> vars) {
        Context context = builder.build();
        if (null != vars) {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                context.getBindings(LANGUAGE_ID).putMember(entry.getKey(), entry.getValue());
            }
        }
        return context.eval(LANGUAGE_ID, script);
    }


}
