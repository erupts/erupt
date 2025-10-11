package xyz.erupt.tpl.engine;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/1/16 20:19
 */

public abstract class EngineTemplate<Engine> {

    @Setter
    @Getter
    private Engine engine;

    public abstract Tpl.Engine engine();

    public abstract Engine init();

    public abstract void render(Engine engine, String filePath, Map<String, Object> bindingMap, Writer out);

}
