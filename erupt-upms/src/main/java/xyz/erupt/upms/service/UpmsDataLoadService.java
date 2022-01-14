package xyz.erupt.upms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.util.UPMSUtil;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

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

    public static final String DEFAULT_ACCOUNT = "erupt";

    @Transactional
    @Override
    public void run(String... args) {
        EruptModuleInvoke.invoke(module -> Optional.ofNullable(module.initMenus()).ifPresent(metaMenus ->
                new ProjectUtil().projectStartLoaded(module.info().getName(), first -> {
                    if (first) {
                        module.initFun();
                        for (MetaMenu metaMenu : metaMenus) {
                            EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, EruptMenu.fromMetaMenu(metaMenu), EruptMenu.CODE, metaMenu.getCode());
                            metaMenu.setId(eruptMenu.getId());
                            if (null != eruptMenu.getType() && null != eruptMenu.getValue()) {
                                if (MenuTypeEnum.TABLE.getCode().equals(eruptMenu.getType()) || MenuTypeEnum.TREE.getCode().equals(eruptMenu.getType())) {
                                    int i = 0;
                                    for (EruptFunPermissions value : EruptFunPermissions.values()) {
                                        eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                                                UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value),
                                                value.getName(), MenuTypeEnum.BUTTON.getCode(),
                                                UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value),
                                                eruptMenu, i += 10
                                        ), EruptMenu.CODE, UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value));
                                    }
                                }
                            }
                        }
                    }
                })
        ));
        new ProjectUtil().projectStartLoaded("erupt-upms-user", first -> {
            if (first) {
                //用户
                if (eruptDao.queryEntityList(EruptUser.class, "isAdmin = true").size() <= 0) {
                    EruptUser eruptUser = new EruptUser();
                    eruptUser.setIsAdmin(true);
                    eruptUser.setIsMd5(true);
                    eruptUser.setStatus(true);
                    eruptUser.setCreateTime(new Date());
                    eruptUser.setAccount(DEFAULT_ACCOUNT);
                    eruptUser.setPassword(MD5Util.digest(DEFAULT_ACCOUNT));
                    eruptUser.setName(DEFAULT_ACCOUNT);
                    eruptUser.setResetPwdTime(new Date());
                    eruptDao.persistIfNotExist(EruptUser.class, eruptUser, "account", eruptUser.getAccount());
                }
            }
        });
    }

}
