package xyz.erupt.upms.handler;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.upms.cache.CaffeineEruptCache;
import xyz.erupt.upms.constant.FetchConst;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/01/03 18:00
 */
@Component
public class SqlChoiceFetchHandler implements ChoiceFetchHandler {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final String CACHE_SPACE = SqlChoiceFetchHandler.class.getName() + ":";

    private final CaffeineEruptCache<List<VLModel>> sqlCache = new CaffeineEruptCache<>();

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException("SqlChoiceFetchHandler → params not found");
        }
        long timeout = FetchConst.DEFAULT_CACHE_TIME;
        if (params.length == 2) {
            timeout = Long.parseLong(params[1]);
        }
        sqlCache.init(timeout);
        return sqlCache.get(CACHE_SPACE + params[0], (key) -> jdbcTemplate.query(params[0], (rs, i) -> {
            if (rs.getMetaData().getColumnCount() == 1) {
                return new VLModel(rs.getString(1), rs.getString(1));
            } else {
                return new VLModel(rs.getString(1), rs.getString(2));
            }
        }));
    }

}
