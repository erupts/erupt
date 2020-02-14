package xyz.erupt.bi.service;

import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.tool.EruptDao;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
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

    private static final String EXPRESS_PATTERN = "(?<=\\$\\{)(.+?)(?=\\})";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private EruptDao eruptDao;

    public static void main(String[] args) throws IOException, ScriptException {
        Resource resource = new ClassPathResource("test.sql");
        String sql = FileUtils.readFileToString(resource.getFile());
        Map<String, Object> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject("{name:['emmm','23333']}");
        map.put("name", new String[]{"emmmmm", "233333"});
        new BiService().processPlaceHolder(sql, map);
    }

    public List<Map<String, Object>> processBiSql(String code, Map<String, Object> query) {
        Bi bi = eruptDao.queryEntity(Bi.class, "code = '" + code + "'");
        try {
            String express = processPlaceHolder(bi.getSqlStatement(), query);
            log.info(bi.getName() + " -> " + express);
            NamedParameterJdbcTemplate jdbcTemplate = getJdbcTemplate(bi.getDataSource());
            return jdbcTemplate.queryForList(express, query);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private NamedParameterJdbcTemplate getJdbcTemplate(BiDataSource biDataSource) {
        if (null == biDataSource) {
            return jdbcTemplate;
        } else {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(biDataSource.getDriver());
            dataSource.setUrl(biDataSource.getUrl());
            dataSource.setPassword(biDataSource.getPassword());
            dataSource.setUsername(biDataSource.getUserName());
            return new NamedParameterJdbcTemplate(dataSource);
        }
    }

    private String processPlaceHolder(String express, Map<String, Object> param) throws ScriptException {
        Matcher m = Pattern.compile(EXPRESS_PATTERN).matcher(express);
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            scriptEngine.put(entry.getKey(), param.get(entry.getKey()));
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
