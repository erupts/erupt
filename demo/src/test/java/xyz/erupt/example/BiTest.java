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
import xyz.erupt.bi.service.BiService;

import java.io.IOException;
import java.util.ArrayList;
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
    private BiService biService;

    @Test
    public void biTest() throws IOException {
        Map<String, Object> paramMap = new HashMap<>();
        Resource resource = new ClassPathResource("test.sql");
        List<String[]> list = new ArrayList<>();
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
        Map<String, Object> paramMap = new HashMap<>();

        List<Map<String, Object>> lm = jdbcTemplate.queryForList(FileUtils.readFileToString(resource.getFile()), paramMap);
    }
}
