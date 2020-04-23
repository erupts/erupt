package xyz.erupt.bi.service;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.fun.BiHandler;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiClassHandler;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tool.EruptDao;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liyuepeng
 * @date 2020-02-12
 */
@Service
@Log
public class BiService {

    @Autowired
    private BiDataSourceService dataSourceService;

    @Autowired
    private EruptDao eruptDao;

    private static final String EXPRESS_PATTERN = "(?<=\\$\\{)(.+?)(?=\\})";

    public Bi findBi(String code) {
        return eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {
            {
                this.put("code", code);
            }
        });
    }

    @SneakyThrows
    public List<Map<String, Object>> startQuery(String express, BiClassHandler classHandler, BiDataSource biDataSource, Map<String, Object> query) {
        BiHandler biHandler = null;
        express = processPlaceHolder(express, query);
        if (null != classHandler) {
            biHandler = EruptSpringUtil.getBeanByPath(classHandler.getHandlerPath(), BiHandler.class);
            express = biHandler.exprHandler(classHandler.getParam(), express);
        }
        NamedParameterJdbcTemplate jdbcTemplate = dataSourceService.getJdbcTemplate(biDataSource);
        List<Map<String, Object>> list = jdbcQuery(jdbcTemplate, express, query);
        if (null != biHandler) {
            biHandler.resultHandler(classHandler.getParam(), list);
        }
        return list;
    }

    private List<Map<String, Object>> jdbcQuery(NamedParameterJdbcTemplate jdbcTemplate, String express, Map<String, Object> query) {
        log.info(express);
        return jdbcTemplate.query(express, query, (rs, i) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int index = 1; index <= columnCount; index++) {
                map.put(metaData.getColumnLabel(index), rs.getObject(index));
            }
            return map;
        });
    }

    @SneakyThrows
    private String processPlaceHolder(String express, Map<String, Object> param) {
        Matcher m = Pattern.compile(EXPRESS_PATTERN).matcher(express);
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("js");
        try {
            for (String s : BiDataLoadService.functions) {
                scriptEngine.eval(s);
            }
        } catch (ScriptException e) {
            throw new RuntimeException("函数脚本解析异常：" + e.getMessage());
        }
        if (null != param) {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                scriptEngine.put(entry.getKey(), param.get(entry.getKey()));
            }
        }
        while (m.find()) {
            String exp = m.group();
            Object result = scriptEngine.eval(exp);
            if (null == result) {
                result = "";
            }
            express = express.replace("${" + exp + "}", result.toString());
        }
        return express;
    }
}
