package xyz.erupt.bi.cube.service;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.cube.annotation.Parameter;
import xyz.erupt.bi.cube.core.CubeModel;
import xyz.erupt.bi.cube.core.DimensionModel;
import xyz.erupt.bi.cube.core.MeasureModel;
import xyz.erupt.bi.cube.query.CubeQuery;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class CubeQueryService {

    private static final VelocityEngine velocityEngine = new VelocityEngine();

    public static final String SELECT = "SELECT ";

    public static final String FROM = " FROM ";

    public static final String WHERE = " WHERE ";

    public static final String GROUP_BY = " GROUP BY ";

    public static final String COMMA = ",";

    static {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        velocityEngine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        velocityEngine.init();
    }

    public void cubeToSql(String cubeName, CubeQuery cubeQuery) {
        CubeModel cubeModel = CubeCoreService.get(cubeName);
        StringBuilder sql = new StringBuilder();
        sql.append(SELECT);
        for (String dimension : cubeQuery.getDimensions()) {
            DimensionModel d = cubeModel.getDimensionMap().get(dimension);
            if ("".equals(d.getDimension().sql())) {
                sql.append(d.getField().getName());
            } else {
                sql.append(d.getDimension().sql());
            }
            sql.append(COMMA);
        }
        for (String measure : cubeQuery.getMeasures()) {
            MeasureModel m = cubeModel.getMeasureMap().get(measure);
            sql.append(m.getMeasure().sql()).append(COMMA);
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(FROM).append(cubeModel.getCube().sql());
        if (!cubeQuery.getFilters().isEmpty()) {
            sql.append(WHERE);
            for (String filter : cubeQuery.getFilters()) {
                sql.append(filter).append(" = ").append(100);
            }
        }
        if (cubeQuery.isGroupBy()) {
            sql.append(GROUP_BY);
            for (String dimension : cubeQuery.getDimensions()) {
                DimensionModel d = cubeModel.getDimensionMap().get(dimension);
                if ("".equals(d.getDimension().sql())) {
                    sql.append(d.getField().getName());
                } else {
                    sql.append(d.getDimension().sql());
                }
                sql.append(COMMA);
            }
            sql.deleteCharAt(sql.length() - 1);
        }
        Map<String, Object> context = new HashMap<>();
        for (Parameter parameter : cubeModel.getCube().parameters()) {
            context.put(parameter.name(), null);
        }
        VelocityContext ctx = new VelocityContext(context);
        StringWriter out = new StringWriter();
        velocityEngine.evaluate(ctx, out, CubeQueryService.class.getSimpleName(), sql.toString());
        String res = out.toString();
        System.out.println(res);
    }

}
