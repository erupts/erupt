package xyz.erupt.bi.cube.service;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.cube.annotation.Cube;
import xyz.erupt.bi.cube.annotation.Parameter;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class CubeService {

    private static final VelocityEngine velocityEngine = new VelocityEngine();

    static {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        velocityEngine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        velocityEngine.init();
    }

    public void cubeToSql(Class<?> cubeClass) {
        Cube cube = cubeClass.getAnnotation(Cube.class);
        StringBuilder sql = new StringBuilder();
        sql.append(cube.sql()).append(" ");
        for (Field field : cubeClass.getDeclaredFields()) {

        }
        Map<String, Object> context = new HashMap<>();
        for (Parameter parameter : cube.parameters()) {
            context.put(parameter.name(), null);
        }
        VelocityContext ctx = new VelocityContext(context);
        StringWriter out = new StringWriter();
        velocityEngine.evaluate(ctx, out, "EruptCube", sql.toString());
        String res = out.toString();
        System.out.println(res);
    }

}
