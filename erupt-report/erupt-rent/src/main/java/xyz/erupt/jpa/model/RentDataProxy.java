package xyz.erupt.jpa.model;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Service
public class RentDataProxy implements DataProxy<BaseModel> {

    @Override
    public void beforeAdd(BaseModel baseModel) {
        throw new RuntimeException("test test test test");
//        if (rentMap.containsKey(eruptUserService.getCurrentUid())) {
//
//        } else {
//            EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
//
//        }
//        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
//        baseModel.getEruptRent();
    }


}
