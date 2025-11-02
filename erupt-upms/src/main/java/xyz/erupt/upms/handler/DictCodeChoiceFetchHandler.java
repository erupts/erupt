package xyz.erupt.upms.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.upms.model.EruptDictItem;

/**
 * @author YuePeng
 * date 2020/12/16 18:00
 */
@Component
public class DictCodeChoiceFetchHandler extends BaseDictChoiceFetchHandler {

    @Override
    protected VLModel createVLModel(EruptDictItem item) {
        return new VLModel(item.getCode(), item.getName());
    }

}
