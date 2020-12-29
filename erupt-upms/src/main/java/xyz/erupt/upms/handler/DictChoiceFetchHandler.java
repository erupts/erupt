package xyz.erupt.upms.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptDictItem;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2020/12/16 18:00
 */
@Component
public class DictChoiceFetchHandler implements ChoiceFetchHandler {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        if (null == params || params.length == 0) {
            throw new RuntimeException("DictChoiceFetchHandler → params[0] must dict → code");
        }
        List<VLModel> list = new ArrayList<>();
        List<EruptDictItem> eruptDictItem = eruptDao.queryEntityList(EruptDictItem.class, "eruptDict.code = '" + params[0] + "'", null);
        for (EruptDictItem item : eruptDictItem) {
            list.add(new VLModel(item.getId().toString(), item.getName()));
        }
        return list;
    }

}
