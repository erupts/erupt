package xyz.erupt.example;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BiTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void biTest() throws IOException {
        Map<String, Object> paramMap = new HashMap<>();
        Resource resource = new ClassPathResource("test.sql");
        List<Map<String, Object>> lm = jdbcTemplate.queryForList(FileUtils.readFileToString(resource.getFile()), paramMap);
        System.out.println(lm);
//        List o = jdbcTemplate.query(FileUtils.readFileToString(resource.getFile()), paramMap, (resultSet, num) -> {
//            ResultSetMetaData metaData = resultSet.getMetaData();
//            String[] strings = new String[metaData.getColumnCount()];
//            for (int i = 0; i < metaData.getColumnCount(); i++) {
//                System.out.println(metaData.getColumnLabel(i + 1));
//                strings[i] = resultSet.getString(i + 1);
//            }
//            return strings;
//        });
//        System.out.println(o);
    }

    @Test
    public void processPlaceHolder() throws IOException {
        Resource resource = new ClassPathResource("test.sql");
        String sql = FileUtils.readFileToString(resource.getFile());
        Map<String, Object> map = new HashMap<>();
        map.put("name", "233");
        map.put("kkkk", "313");
        List list = jdbcTemplate.queryForList("select * from demo where name = :name", map);
        System.out.println(list);
    }

    @Test
    public void aa() {
        ScriptEngineManager manager = new ScriptEngineManager();
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            System.out.printf("language: %s, engine: %s%n", factory.getLanguageName(), factory.getEngineName());
        }
        ScriptEngine engine = manager.getEngineByName("SQL");
        try {
            Object result = engine.eval("SELECT 1+2;");
            System.out.println(result);
        } catch (ScriptException ex) {
            System.out.println(ex);
        }
    }


}
