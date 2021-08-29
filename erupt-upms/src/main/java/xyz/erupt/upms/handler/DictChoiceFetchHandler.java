package xyz.erupt.upms.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.util.EruptAssert;
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

    @Resource
    private EruptDao eruptDao;

    private final CaffeineEruptCache<List<VLModel>> dictCache = new CaffeineEruptCache<>();

    @Override
    public List<VLModel> fetch(String[] params) {
        EruptAssert.notNull(params, DictChoiceFetchHandler.class.getSimpleName() + " → params[0] must dict → code");
        dictCache.init(params.length == 2 ? Long.parseLong(params[1]) : FetchConst.DEFAULT_CACHE_TIME);
        return dictCache.get(DictChoiceFetchHandler.class.getName() + ":" + params[0], (key) ->
                eruptDao.queryEntityList(EruptDictItem.class, "eruptDict.code = '" + params[0] + "'")
                        .stream().map((item) -> new VLModel(item.getId(), item.getName())).collect(Collectors.toList()));
    }

}
