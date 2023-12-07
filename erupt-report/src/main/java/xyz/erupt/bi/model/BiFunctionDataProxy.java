package xyz.erupt.bi.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.service.ScriptService;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.util.Erupts;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author YuePeng
 * date 2023/6/4 17:58
 */
@Component
public class BiFunctionDataProxy implements DataProxy<BiFunction> {

    private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(BiConst.SCRIPT_ENGINE);
    @Resource
    private ScriptService scriptService;

    private void testFunction(BiFunction biFunction) {
        try {
            scriptEngine.eval(biFunction.getJsFunction());
        } catch (ScriptException e) {
            throw new EruptApiErrorTip(e.getMessage());
        }
    }

    @Override
    public void beforeAdd(BiFunction biFunction) {
        biFunction.setCode(Erupts.generateCode());
    }

    @Override
    public void afterAdd(BiFunction biFunction) {
        this.testFunction(biFunction);
        scriptService.flushFunction();
    }

    @Override
    public void afterUpdate(BiFunction biFunction) {
        this.afterAdd(biFunction);
    }

    @Override
    public void afterDelete(BiFunction biFunction) {
        scriptService.flushFunction();
    }

}
