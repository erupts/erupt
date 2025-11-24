package xyz.erupt.bi.cube.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.bi.cube.annotation.Parameter;
import xyz.erupt.bi.cube.constant.SqlConst;
import xyz.erupt.bi.cube.constant.SqlType;
import xyz.erupt.bi.cube.core.CubeModel;
import xyz.erupt.bi.cube.core.DimensionModel;
import xyz.erupt.bi.cube.core.MeasureModel;
import xyz.erupt.bi.cube.pojo.query.CubeFilter;
import xyz.erupt.bi.cube.pojo.query.CubeQuery;
import xyz.erupt.bi.cube.pojo.query.SqlParameter;
import xyz.erupt.bi.cube.pojo.vo.CubeResultVo;
import xyz.erupt.core.util.ScriptUtil;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CubeQueryService {

    private static final VelocityEngine velocityEngine = new VelocityEngine();

    @Resource
    private NamedParameterJdbcTemplate jdbcTemplate;

    static {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        velocityEngine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        velocityEngine.init();
    }

    public SqlParameter cubeToSql(String cubeName, CubeQuery cubeQuery) {
        Map<String, Object> context = new HashMap<>();
        CubeModel cubeModel = CubeCoreService.get(cubeName);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlConst.SELECT);
        for (String dimension : cubeQuery.getDimensions()) {
            DimensionModel dim = cubeModel.getDimensionMap().get(dimension);
            if ("".equals(dim.getDimension().sql())) {
                sql.append(dim.getField().getName());
            } else {
                sql.append(dim.getDimension().sql());
            }
            sql.append(SqlConst.COMMA);
        }
        for (String measure : cubeQuery.getMeasures()) {
            MeasureModel m = cubeModel.getMeasureMap().get(measure);
            sql.append(m.getMeasure().sql()).append(SqlConst.COMMA);
        }
        sql.deleteCharAt(sql.length() - SqlConst.COMMA.length());
        sql.append(SqlConst.FROM);
        if (cubeModel.getCube().sqlType() == SqlType.TABLE_NAME) {
            sql.append(cubeModel.getCube().sql()).append(SqlConst.AS).append(cubeModel.getClazz().getSimpleName());
        } else if (cubeModel.getCube().sqlType() == SqlType.SUB_QUERY) {
            sql.append("(").append(cubeModel.getCube().sql()).append(")").append(SqlConst.AS).append(cubeModel.getClazz().getSimpleName());
        }
        if (!cubeQuery.getFilters().isEmpty()) {
            sql.append(SqlConst.WHERE);
            for (CubeFilter cubeFilter : cubeQuery.getFilters()) {
                context.put(cubeFilter.getField(), cubeFilter.getValue());
                sql.append(cubeFilter.getField()).append(" = ").append(cubeFilter.getValue());
            }
        }
        if (cubeQuery.isGroupBy()) {
            sql.append(SqlConst.GROUP_BY);
            for (String dimension : cubeQuery.getDimensions()) {
                DimensionModel dim = cubeModel.getDimensionMap().get(dimension);
                if ("".equals(dim.getDimension().sql())) {
                    sql.append(dim.getField().getName());
                } else {
                    sql.append(dim.getDimension().sql());
                }
                sql.append(SqlConst.COMMA);
            }
            sql.deleteCharAt(sql.length() - SqlConst.COMMA.length());
        }
        for (Parameter parameter : cubeModel.getCube().parameters()) {
            context.put(parameter.name(), null);
        }
        VelocityContext ctx = new VelocityContext(context);
        StringWriter out = new StringWriter();
        velocityEngine.evaluate(ctx, out, CubeQueryService.class.getSimpleName(), sql.toString());
        String res = out.toString();
        log.info("Cube SQL {}", res);
        SqlParameter sqlParameter = new SqlParameter();
        sqlParameter.setSql(res);
        sqlParameter.setParameters(context);
        return sqlParameter;
    }

    public List<Map<String, CubeResultVo>> executeCubeSql(String cubeName, CubeQuery cubeQuery, SqlParameter sqlParameter) {
        CubeModel cubeModel = CubeCoreService.get(cubeName);
        @SuppressWarnings("SqlSourceToSinkFlow")
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlParameter.getSql(), sqlParameter.getParameters());
        List<Map<String, CubeResultVo>> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, CubeResultVo> resultVoMap = new HashMap<>();
            result.add(resultVoMap);
            cubeQuery.getDimensions().forEach(dim -> {
                DimensionModel dimensionModel = cubeModel.getDimensionMap().get(dim);
                if (!AnnotationConst.EMPTY_STR.equals(dimensionModel.getDimension().format())) {
                    Map<String, Object> context = new HashMap<>();
                    context.put("value", map.get(dim));
                    resultVoMap.put(dim, new CubeResultVo(map.get(dim), ScriptUtil.eval(dimensionModel.getDimension().format(), context, Object.class)));
                } else {
                    resultVoMap.put(dim, new CubeResultVo(map.get(dim)));
                }
            });
            cubeQuery.getMeasures().forEach(measure -> {
                MeasureModel measureModel = cubeModel.getMeasureMap().get(measure);
                if (!AnnotationConst.EMPTY_STR.equals(measureModel.getMeasure().format())) {
                    Map<String, Object> context = new HashMap<>();
                    context.put("value", map.get(measure));
                    resultVoMap.put(measure, new CubeResultVo(map.get(measure), ScriptUtil.eval(measureModel.getMeasure().format(), context, Object.class)));
                } else {
                    resultVoMap.put(measure, new CubeResultVo(map.get(measure)));
                }
            });
        }
        return result;
    }

}
