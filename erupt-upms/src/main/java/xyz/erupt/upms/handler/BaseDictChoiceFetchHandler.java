package xyz.erupt.upms.handler;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.cache.EruptCache;
import xyz.erupt.core.cache.EruptCacheLRU;
import xyz.erupt.core.util.EruptAssert;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.constant.FetchConst;
import xyz.erupt.upms.model.EruptDictItem;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for dictionary choice fetch handlers
 * @author YuePeng
 * date 2020/12/16 18:00
 */
public abstract class BaseDictChoiceFetchHandler implements ChoiceFetchHandler {

    @Resource
    private EruptDao eruptDao;

    private final EruptCache<List<VLModel>> dictCache = new EruptCacheLRU<>(500);

    /**
     * Create VLModel from EruptDictItem
     * @param item the dictionary item
     * @return VLModel instance
     */
    protected abstract VLModel createVLModel(EruptDictItem item);

    @Override
    public List<VLModel> fetch(String[] params) {
        EruptAssert.notNull(params, this.getClass().getSimpleName() + " → params[0] must dict → code");
        return dictCache.getAndSet(this.getClass().getName() + ":" + params[0],
                params.length == 2 ? Long.parseLong(params[1]) : FetchConst.DEFAULT_CACHE_TIME, () ->
                        eruptDao.lambdaQuery(EruptDictItem.class).addCondition("eruptDict.code = :code",
                                        new HashMap<String, Object>() {{
                                            this.put("code", params[0]);
                                        }}).orderBy(EruptDictItem::getSort).list()
                                .stream().map(this::createVLModel)
                                .collect(Collectors.toList()));
    }
}
