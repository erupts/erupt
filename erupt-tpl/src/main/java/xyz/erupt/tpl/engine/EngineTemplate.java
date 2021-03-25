package xyz.erupt.tpl.engine;

import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/1/16 20:19
 */
public abstract class EngineTemplate<Engine> {

    private Engine engine;

    public abstract Tpl.Engine engine();

    public abstract Engine init();

    public abstract void render(Engine engine, String filePath, Map<String, Object> bindingMap, Writer out);

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
