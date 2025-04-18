package xyz.erupt.upms.model.data_proxy;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.upms.model.EruptOpenApi;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.service.EruptUserService;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2024/8/11 15:37
 */
@Component
public class EruptOpenApiDataProxy implements DataProxy<EruptOpenApi>, OperationHandler<EruptOpenApi, Void> {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptTokenService eruptTokenService;

    @Override
    public void beforeAdd(EruptOpenApi eruptOpenApi) {
        eruptOpenApi.setAppid("es" + RandomStringUtils.random(14, EruptConst.AN));
        eruptOpenApi.setSecret(RandomStringUtils.randomAlphanumeric(24).toUpperCase());
    }

    @Override
    public void afterUpdate(EruptOpenApi eruptOpenApi) {
        if (!eruptOpenApi.getStatus()) {
            this.logoutToken(eruptOpenApi);
            eruptOpenApi.setCurrentToken(null);
        }
    }

    @Override
    public void afterDelete(EruptOpenApi eruptOpenApi) {
        this.logoutToken(eruptOpenApi);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String secretField = LambdaSee.field(EruptOpenApi::getSecret);
        for (Map<String, Object> map : list) {
            String secret = map.get(secretField).toString();
            map.put(secretField, secret.substring(0, 4) + "********" + secret.substring(secret.length() - 4));
        }
    }

    private void logoutToken(EruptOpenApi eruptOpenApi) {
        Optional.ofNullable(eruptOpenApi.getCurrentToken()).ifPresent(it -> eruptTokenService.logoutToken(eruptOpenApi.getName(), eruptOpenApi.getCurrentToken()));
    }

    @Override
    @Transactional
    public String exec(List<EruptOpenApi> data, Void unused, String[] param) {
        EruptOpenApi eruptOpenApi = eruptDao.find(EruptOpenApi.class, data.get(0).getId());
        eruptOpenApi.setSecret(RandomStringUtils.randomAlphanumeric(24).toUpperCase());
        return "msg.info('new secret:" + eruptOpenApi.getSecret() + "')";
    }

}
