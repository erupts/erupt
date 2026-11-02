package xyz.erupt.config.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * @author YuePeng
 */
@Component
public class ConfigProfileFetchHandler implements ChoiceFetchHandler<Object> {

    private final EruptDao eruptDao;

    public ConfigProfileFetchHandler(EruptDao eruptDao) {
        this.eruptDao = eruptDao;
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        return eruptDao.lambdaQuery(ConfigProfile.class).orderBy(ConfigProfile::getId).list().stream()
                .map(it -> new VLModel(String.valueOf(it.getId()), it.getCode())).toList();
    }

}
