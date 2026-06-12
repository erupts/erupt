package xyz.erupt.print.service;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.springframework.stereotype.Service;
import xyz.erupt.print.var.PrintVar;

import java.io.StringWriter;
import java.util.Map;

@Service
public class EruptPrintService {

    private static final VelocityEngine velocityEngine = new VelocityEngine();

    static {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, String.class.getSimpleName());
        velocityEngine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        velocityEngine.init();
    }

    public String render(String template, Map<String, Object> vars) {
        VelocityContext ctx = new VelocityContext(vars);
        for (PrintVar value : PrintVar.PRINT_VAR_MAP.values()) {
            ctx.put(value.code(), value.value());
        }
        StringWriter out = new StringWriter();
        velocityEngine.evaluate(ctx, out, EruptPrintService.class.getSimpleName(), template);
        return out.toString();
    }

}
