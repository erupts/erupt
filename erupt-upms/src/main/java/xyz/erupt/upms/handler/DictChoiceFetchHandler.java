package xyz.erupt.upms.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.cache.CaffeineEruptCache;
import xyz.erupt.upms.constant.FetchConst;
import xyz.erupt.upms.model.EruptDictItem;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2020/12/16 18:00
 */
@Component
public class DictChoiceFetchHandler implements ChoiceFetchHandler {

    private final String CACHE_SPACE = DictChoiceFetchHandler.class.getName() + ":";

    private final CaffeineEruptCache<List<VLModel>> dictCache = new CaffeineEruptCache<>();

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException(DictChoiceFetchHandler.class.getSimpleName() + " → params[0] must dict → code");
        }
        dictCache.init(params.length == 2 ? Long.parseLong(params[1]) : FetchConst.DEFAULT_CACHE_TIME);
        return dictCache.get(CACHE_SPACE + params[0], (key) ->
                eruptDao.queryEntityList(EruptDictItem.class, "eruptDict.code = '" + params[0] + "'", null)
                        .stream().map((item) -> new VLModel(item.getId(), item.getName())).collect(Collectors.toList()));
    }

}
