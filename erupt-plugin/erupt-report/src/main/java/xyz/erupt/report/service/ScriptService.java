package xyz.erupt.report.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.cache.EruptCache;
import xyz.erupt.core.cache.EruptCacheLRU;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.report.constant.ReportConst;
import xyz.erupt.report.model.BiFunction;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

/**
 * @author YuePeng
 * date 2023/11/30 00:00
 */
@Service
public class ScriptService {

    public static final String FUNCTION_CACHE_KEY = "bi-function-cache-key";

    private final EruptCache<String> functionCache = new EruptCacheLRU<>(1);

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(ReportConst.SCRIPT_ENGINE);

    @Resource
    private EruptDao eruptDao;

    public Object eval(String script, Bindings bindings) throws ScriptException {
        String fun = this.findFunction();
        if (null != fun) {
            if (null == bindings) {
                scriptEngine.eval(fun);
            } else {
                scriptEngine.eval(fun, bindings);
            }
        }
        return null == bindings ? scriptEngine.eval(script) : scriptEngine.eval(script, bindings);
    }

    public Object eval(String script) throws ScriptException {
        return eval(script, null);
    }

    public void flushFunction() {
        functionCache.delete(FUNCTION_CACHE_KEY);
    }

    public String findFunction() {
        return functionCache.getAndSet(FUNCTION_CACHE_KEY, 1500, () -> {
            List<String> list = eruptDao.lambdaQuery(BiFunction.class).listSelect(BiFunction::getJsFunction);
            StringBuilder sb = new StringBuilder();
            for (String fun : list) {
                sb.append(fun).append("\n");
            }
            return sb.toString();
        });
    }

}
