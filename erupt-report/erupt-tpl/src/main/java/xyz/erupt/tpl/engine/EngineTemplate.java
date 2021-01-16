package xyz.erupt.tpl.engine;

import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2021/1/16 09:44
 */
public interface EngineTemplate<Engine> {

    Tpl.Engine engine();

    Engine init();

    void render(Engine engine, String filePath, Map<String, Object> bindingMap, Writer out);

}
