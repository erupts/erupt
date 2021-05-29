package xyz.erupt.upms.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.cache.CaffeineEruptCache;
import xyz.erupt.upms.constant.FetchConst;
import xyz.erupt.upms.model.EruptDictItem;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2020/12/16 18:00
 */
@Component
public class DictCodeChoiceFetchHandler implements ChoiceFetchHandler {

    private final String CACHE_SPACE = DictCodeChoiceFetchHandler.class.getName() + ":";
    private final CaffeineEruptCache<List<VLModel>> dictCache = new CaffeineEruptCache<>();
    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException(DictCodeChoiceFetchHandler.class.getSimpleName() + " → params[0] must dict → code");
        }
        long timeout = FetchConst.DEFAULT_CACHE_TIME;
        if (params.length == 2) {
            timeout = Long.parseLong(params[1]);
        }
        dictCache.init(timeout);
        String dictCode = params[0];
        return dictCache.get(CACHE_SPACE + params[0], (key) -> {
            List<VLModel> list = new ArrayList<>();
            List<EruptDictItem> eruptDictItem = eruptDao.queryEntityList(EruptDictItem.class, "eruptDict.code = '" + dictCode + "'", null);
            for (EruptDictItem item : eruptDictItem) {
                list.add(new VLModel(item.getCode(), item.getName()));
            }
            return list;
        });
    }

}
