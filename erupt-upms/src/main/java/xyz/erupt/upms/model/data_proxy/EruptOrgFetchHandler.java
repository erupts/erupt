package xyz.erupt.upms.model.data_proxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOrg;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/9/26 21:29
 */
@Component
public class EruptOrgFetchHandler implements ChoiceFetchHandler {

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<VLModel> fetch(String[] params) {
        return eruptDao.lambdaQuery(EruptOrg.class).orderBy(EruptOrg::getSort).list().stream().map(
                item -> new VLModel(item.getId(), item.getName() + (null != item.getParentOrg() ? " / " + item.getParentOrg().getName() : ""))
        ).toList();
    }
}
