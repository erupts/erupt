package xyz.erupt.core.util;

import lombok.SneakyThrows;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScriptUtil {

    private static final ConcurrentLinkedQueue<ScriptEngine> POOL = new ConcurrentLinkedQueue<>();

    @SneakyThrows
    public static Object eval(String script, Map<String, Object> vars) {
        ScriptEngine engine = POOL.poll();
        if (engine == null) engine = new ScriptEngineManager().getEngineByName("graal.js");
        try {
            Bindings b = engine.createBindings();
            b.putAll(vars);
            return engine.eval(script, b);
        } finally {
            POOL.offer(engine);
        }
    }


}
