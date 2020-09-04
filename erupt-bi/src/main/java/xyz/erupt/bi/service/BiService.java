package xyz.erupt.bi.service;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.service.EruptUserService;
import xyz.erupt.bi.fun.BiHandler;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiClassHandler;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.bi.view.BiColumn;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.util.EruptSpringUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.ResultSetMetaData;
import java.util.*;
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

    //导出标识符
    private static final String EXPORT_PLACEHOLDER = "$export";

    private static final String EXPRESS_PATTERN = "(?<=\\$\\{)(.+?)(?=\\})";

    public Bi findBi(String code) {
        return eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {
            {
                this.put("code", code);
            }
        });
    }

    private static final String TOTAL_KEY = "count";
    //用户ID
    private static final String USER_ID_PLACEHOLDER = "$uid";
    @Autowired
    private EruptUserService eruptUserService;

    public BiData queryBiData(String code, int pageIndex, int pageSize,
                              Map<String, Object> query, boolean export) {
        Bi bi = findBi(code);
        if (StringUtils.isBlank(bi.getSqlStatement())) {
            throw new RuntimeException("express not found");
        }
        query.put(EXPORT_PLACEHOLDER, export);
        query.put(USER_ID_PLACEHOLDER, eruptUserService.getCurrentUid());
        BiData biData = new BiData();
        if (!export) {
            biData.setTotal(this.getTotal(bi, query));
        }
        if (null == biData.getTotal() || biData.getTotal() > 0) {
            String sql = String.format("select * from (%s) _ limit %s,%s",
                    bi.getSqlStatement(), (pageIndex - 1) * pageSize, pageSize);
            List<Map<String, Object>> list = startQuery(sql, bi.getClassHandler(), bi.getDataSource(), query);
            biData.setList(list);
            if (null != list && list.size() > 0) {
                List<BiColumn> biColumns = new LinkedList<>();
                Map<String, Object> map = list.get(0);
                for (String key : map.keySet()) {
                    BiColumn biColumn = new BiColumn();
                    biColumn.setName(key);
                    biColumns.add(biColumn);
                }
                biData.setColumns(biColumns);
            }
        } else {
            biData.setList(new ArrayList<>(0));
        }
        return biData;
    }

    @SneakyThrows
    private Long getTotal(Bi bi, Map<String, Object> query) {
        String express = processPlaceHolder(bi.getSqlStatement(), query);
        BiClassHandler biDataSource = bi.getClassHandler();
        if (null != biDataSource) {
            express = EruptSpringUtil.getBeanByPath(biDataSource.getHandlerPath(), BiHandler.class)
                    .exprHandler(biDataSource.getParam(), express);
        }
        express = String.format("select count(*) %s from (%s) _", TOTAL_KEY, express);
        NamedParameterJdbcTemplate jdbcTemplate = dataSourceService.getJdbcTemplate(bi.getDataSource());
        return (Long) jdbcTemplate.queryForMap(express, query).get(TOTAL_KEY);
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
