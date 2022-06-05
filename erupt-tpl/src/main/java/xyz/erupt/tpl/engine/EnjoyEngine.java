package xyz.erupt.tpl.engine;

import com.jfinal.kit.StrKit;
import com.jfinal.template.Engine;
import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.util.Map;

/**
 *
 * author: icefairy
 */
public class EnjoyEngine extends EngineTemplate<Engine> {

    @Override
    public Tpl.Engine engine() {
        return Tpl.Engine.Enjoy;
    }

    @Override
    public Engine init() {
        Engine eng = new Engine();
        eng.addSharedMethod(StrKit.class);
        eng.setDevMode(true);
        return eng;
    }

    @Override
    public void render(Engine engine, String filePath, Map<String, Object> bindingMap, Writer out) {
        engine.getTemplate(filePath).render(bindingMap, out);
    }
}
