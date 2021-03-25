package xyz.erupt.tpl.engine;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import xyz.erupt.annotation.sub_erupt.Tpl;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/1/16 22:49
 */
public class VelocityTplEngine extends EngineTemplate<VelocityEngine> {

    @Override
    public Tpl.Engine engine() {
        return Tpl.Engine.Velocity;
    }

    @Override
    public VelocityEngine init() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(Velocity.INPUT_ENCODING, StandardCharsets.UTF_8);
        ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init();
        return ve;
    }

    @Override
    public void render(VelocityEngine velocityEngine, String filePath, Map<String, Object> bindingMap, Writer out) {
        velocityEngine.getTemplate(filePath, StandardCharsets.UTF_8.name()).merge(new VelocityContext(bindingMap), out);
    }
}
