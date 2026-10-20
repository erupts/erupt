package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.upms.constant.EncryptType;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.Date;

/**
 * @author YuePeng
 * date 2019-07-15.
 */
@Service
@Order
@Slf4j
public class UpmsDataLoadService implements CommandLineRunner {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptMenuSeeder eruptMenuSeeder;

    @Resource
    private EruptModuleStore eruptModuleStore;

    private static final String UPMS_USER = "erupt-upms-user";

    @Transactional
    @Override
    public void run(String... args) {
        if (eruptProp.isInitModulesMenu()) eruptMenuSeeder.seedAll();
        // Once per database: a default account that came back whenever no admin was left (a marker
        // file lost with the container) would reopen a well-known password
        eruptModuleStore.once(UPMS_USER, () -> {
            if (eruptModuleStore.legacyMarked(UPMS_USER)) return;
            if (eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getIsAdmin, true).list().isEmpty()) {
                EruptUser eruptUser = new EruptUser();
                eruptUser.setIsAdmin(true);
                eruptUser.setEncrypt(true);
                eruptUser.setStatus(true);
                eruptUser.setCreateTime(new Date());
                eruptUser.setAccount(eruptUpmsProp.getDefaultAccount());
                String salt = EncryptUtil.generateSalt();
                eruptUser.setSalt(salt);
                eruptUser.setEncryptType(EncryptType.SHA512);
                eruptUser.setPassword(EncryptUtil.digestSHA512Salt(eruptUpmsProp.getDefaultPassword(), salt));
                eruptUser.setName(eruptUpmsProp.getDefaultAccount());
                eruptDao.persistIfNotExist(EruptUser.class, eruptUser, LambdaSee.field(EruptUser::getAccount), eruptUser.getAccount());
            }
        });
    }

}