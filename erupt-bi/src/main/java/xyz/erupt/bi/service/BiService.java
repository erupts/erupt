package xyz.erupt.bi.service;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liyuepeng
 * @date 2020-02-12
 */
@Service
public class BiService {

    private static final String EXPRESS_PATTERN = "(?<=\\$\\{)(.+?)(?=\\})";
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public static void main(String[] args) throws IOException, ScriptException {
        Resource resource = new ClassPathResource("test.sql");
        String sql = FileUtils.readFileToString(resource.getFile());
        Map<String, Object> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject("{name:['emmm','23333']}");
        map.put("name", new String[]{"emmmmm", "233333"});
        new BiService().processPlaceHolder(sql, map);
    }

    private String processPlaceHolder(String express, Map<String, Object> param) throws ScriptException {
        Pattern pattern = Pattern.compile(EXPRESS_PATTERN);
        Matcher m = pattern.matcher(express);
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            scriptEngine.put(entry.getKey(), param.get(entry.getKey()));
        }
        //添加自定义函数
        scriptEngine.eval("function exp(n){return 'print:'+n}");
        while (m.find()) {
            String exp = m.group();
            Object result = scriptEngine.eval(exp);
            if (null == result) {
                result = "";
            }
            express = express.replace("${" + exp + "}", result.toString());
        }
        System.out.println(express);
        return express;
    }
}
