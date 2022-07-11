package xyz.erupt.bi.service;

import org.springframework.stereotype.Service;
import xyz.erupt.bi.model.BiFunction;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/4/12 23:12
 */
@Service
public class FunctionService {

    @Resource
    private EruptDao eruptDao;

    public void flushFunction() {
//        List<Object[]> list = eruptDao.queryObjectList(BiFunction.class, null, null, "jsFunction");
//        StringBuilder sb = new StringBuilder();
//        for (Object o : list) {
//            sb.append((String) o).append("\n");
//        }
//        stringRedisTemplate.opsForValue().set(eruptProp.getAppSpacePrefix() + "erupt-bi-function", getFunction());
    }

    public String getFunction() {
        List<Object[]> list = eruptDao.queryObjectList(BiFunction.class, null, null, "jsFunction");
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            sb.append((String) o).append("\n");
        }
        return sb.toString();
    }

}
