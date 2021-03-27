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
public class DictChoiceFetchHandler implements ChoiceFetchHandler {

    @Resource
    private EruptDao eruptDao;

    private final String CACHE_SPACE = DictChoiceFetchHandler.class.getName() + ":";

    private final CaffeineEruptCache<List<VLModel>> dictCache = new CaffeineEruptCache<>();

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException("DictChoiceFetchHandler → params[0] must dict → code");
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
                list.add(new VLModel(item.getId().toString(), item.getName()));
            }
            return list;
        });
    }

}
