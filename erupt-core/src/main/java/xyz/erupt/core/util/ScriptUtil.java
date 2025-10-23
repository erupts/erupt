package xyz.erupt.core.util;

import lombok.SneakyThrows;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

public class ScriptUtil {

    private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");

    @SneakyThrows
    public static <T> T eval(String script, Map<String, Object> vars, Class<T> target) {
        Bindings bindings = scriptEngine.createBindings();
        if (null != vars) bindings.putAll(vars);
        return (T) scriptEngine.eval(script, bindings);
    }


}
