package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.prop.InitMethodEnum;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.upms.constant.EncryptType;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.util.UPMSUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Transactional
    @Override
    public void run(String... args) {
        if (eruptDao.lambdaQuery(EruptUser.class).count() <= 0) {
            try {
                FileUtils.deleteDirectory(new File(EruptConst.ERUPT_DIR_PATH));
            } catch (IOException e) {
                log.error("Table 'e_upms_user' no user data. Re-initialization failed ：", e);
            }
        }
        if (InitMethodEnum.NONE != eruptProp.getInitMethodEnum()) {
            EruptModuleInvoke.invoke(module -> Optional.ofNullable(module.initMenus()).ifPresent(metaMenus ->
                    new ProjectUtil().projectStartLoaded(module.info().getName(), first -> {
                        Runnable runnable = (() -> {
                            module.initFun();
                            for (MetaMenu metaMenu : metaMenus) {
                                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.fromMetaMenu(metaMenu), LambdaSee.field(EruptMenu::getCode), metaMenu.getCode());
                                metaMenu.setId(eruptMenu.getId());
                                if (null != eruptMenu.getType() && null != eruptMenu.getValue()) {
                                    EruptFunPermissions[] permissions = EruptFunPermissions.byMenuType(eruptMenu.getType());
                                    if (null != permissions) {
                                        AtomicInteger i = new AtomicInteger();
                                        Optional.ofNullable(EruptCoreService.getErupt(eruptMenu.getValue())).ifPresent(it -> {
                                            Power power = it.getErupt().power();
                                            for (EruptFunPermissions value : permissions) {
                                                if (value.verifyPower(power)) {
                                                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                                                            UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value),
                                                            value.getName(), MenuTypeEnum.BUTTON.getCode(),
                                                            UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value),
                                                            eruptMenu, i.addAndGet(10)
                                                    ), LambdaSee.field(EruptMenu::getCode), UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value));
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                        switch (eruptProp.getInitMethodEnum()) {
                            case EVERY:
                                runnable.run();
                                break;
                            case FILE:
                                if (first) runnable.run();
                                break;
                        }
                    }, this::runAfterCommit)
            ));
        }
        new ProjectUtil().projectStartLoaded("erupt-upms-user", first -> {
            if (first) {
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
            }
        }, this::runAfterCommit);
    }

    // The seeding above runs inside the run() transaction while the .loaded marker is a plain file
    // write; writing it only after commit keeps a rollback from leaving a marker without its data
    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

}