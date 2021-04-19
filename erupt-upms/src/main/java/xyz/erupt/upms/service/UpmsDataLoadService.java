package xyz.erupt.upms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import xyz.erupt.core.util.ProjectUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.MenuStatus;
import xyz.erupt.upms.enums.MenuTypeEnum;
import xyz.erupt.upms.model.*;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.model.log.EruptOperateLog;
import xyz.erupt.upms.util.MD5Utils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
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

    public static final String DEFAULT_ACCOUNT = "erupt";

    @Transactional
    @Override
    public void run(String... args) {
        new ProjectUtil().projectStartLoaded("upms", first -> {
            if (first) {
                //用户
                if (eruptDao.queryEntityList(EruptUser.class, "isAdmin = true").size() <= 0) {
                    EruptUser eruptUser = new EruptUser();
                    eruptUser.setIsAdmin(true);
                    eruptUser.setIsMd5(true);
                    eruptUser.setStatus(true);
                    eruptUser.setCreateTime(new Date());
                    eruptUser.setAccount(DEFAULT_ACCOUNT);
                    eruptUser.setPassword(MD5Utils.digest(DEFAULT_ACCOUNT));
                    eruptUser.setName(DEFAULT_ACCOUNT);
                    eruptDao.persistIfNotExist(EruptUser.class, eruptUser, "account", eruptUser.getAccount());
                }
                //菜单
                String code = "code";
                String manager = "$manager";
                Integer open = MenuStatus.OPEN.getValue();
                EruptMenu eruptMenu = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(manager, "系统管理", null, null, 1, 0, "fa fa-cogs", null)
                        , code, manager);
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        EruptMenu.class.getSimpleName(), "菜单维护", MenuTypeEnum.TREE.getCode(), EruptMenu.class.getSimpleName(), open, 10, "fa fa-list-ul", eruptMenu
                ), code, EruptMenu.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        EruptRole.class.getSimpleName(), "角色维护", MenuTypeEnum.TABLE.getCode(), EruptRole.class.getSimpleName(), open, 20, null, eruptMenu
                ), code, EruptRole.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        EruptOrg.class.getSimpleName(), "组织维护", MenuTypeEnum.TREE.getCode(), EruptOrg.class.getSimpleName(), open, 30, "fa fa-users", eruptMenu
                ), code, EruptOrg.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        EruptPost.class.getSimpleName(), "岗位维护", MenuTypeEnum.TABLE.getCode(), EruptPost.class.getSimpleName(), open, 35, "fa fa-id-card", eruptMenu
                ), code, EruptPost.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        EruptUser.class.getSimpleName(), "用户维护", MenuTypeEnum.TABLE.getCode(), EruptUser.class.getSimpleName(), open, 40, "fa fa-user", eruptMenu
                ), code, EruptUser.class.getSimpleName());
                {
                    EruptMenu eruptMenuDict = eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                            EruptDict.class.getSimpleName(), "字典维护", MenuTypeEnum.TABLE.getCode(), EruptDict.class.getSimpleName(), open, 50, null, eruptMenu
                    ), code, EruptDict.class.getSimpleName());
                    eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                            EruptDictItem.class.getSimpleName(), "字典项", MenuTypeEnum.TABLE.getCode(), EruptDictItem.class.getSimpleName(), MenuStatus.HIDE.getValue(), 10, null, eruptMenuDict
                    ), code, EruptDictItem.class.getSimpleName());
                }
                eruptDao.persistIfNotExist(EruptMenu.class, new EruptMenu(
                        EruptLoginLog.class.getSimpleName(), "登录日志", MenuTypeEnum.TABLE.getCode(), EruptLoginLog.class.getSimpleName(), open, 60, null, eruptMenu
                ), code, EruptLoginLog.class.getSimpleName());
                eruptDao.persistIfNotExist(EruptOperateLog.class, new EruptMenu(
                        EruptOperateLog.class.getSimpleName(), "操作日志", MenuTypeEnum.TABLE.getCode(), EruptOperateLog.class.getSimpleName(), open, 70, null, eruptMenu
                ), code, EruptOperateLog.class.getSimpleName());
            } else {
                EruptUser eruptUser = eruptDao.queryEntity(EruptUser.class, "account = '" + DEFAULT_ACCOUNT + "'", null);
                if (null != eruptUser) {
                    String password = eruptUser.getPassword();
                    if (!eruptUser.getIsMd5()) {
                        password = MD5Utils.digest(eruptUser.getPassword());
                    }
                    if (MD5Utils.digest(DEFAULT_ACCOUNT).equals(password)) {
                        log.warn("正在使用框架默认用户名密码，为了您的系统安全请尽快修改！");
                    }
                }
            }
        });
    }

}
