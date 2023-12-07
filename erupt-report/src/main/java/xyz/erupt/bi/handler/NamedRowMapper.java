package xyz.erupt.bi.handler;


import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2023/12/5 22:13
 */
public class NamedRowMapper implements RowMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> map = new LinkedHashMap<>(columnCount);
        for (int index = 1; index <= columnCount; index++) {
            map.put(metaData.getColumnLabel(index), rs.getObject(index));
        }
        return map;
    }
}
