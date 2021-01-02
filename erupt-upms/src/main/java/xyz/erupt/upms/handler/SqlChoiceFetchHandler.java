package xyz.erupt.upms.handler;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2020/12/16 18:00
 */
@Component
public class SqlChoiceFetchHandler implements ChoiceFetchHandler {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException("SqlChoiceFetchHandler → params not found");
        }
        return jdbcTemplate.query(params[0], (rs, i) -> {
            int cc = rs.getMetaData().getColumnCount();
            if (cc == 1) {
                return new VLModel(rs.getString(1), rs.getString(1));
            } else {
                return new VLModel(rs.getString(1), rs.getString(2));
            }
        });
    }

}
