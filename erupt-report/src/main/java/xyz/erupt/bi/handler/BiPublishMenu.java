package xyz.erupt.bi.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.bi.config.EruptBiProp;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiReleaseModal;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author YuePeng
 * date 2023/6/4 17:51
 */
@Component
public class BiPublishMenu implements OperationHandler<Bi, BiReleaseModal> {

    @Resource
    @Transient
    private EruptDao eruptDao;

    @Resource
    @Transient
    private EruptBiProp eruptBiProp;

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Resource
    @Transient
    private EruptContextService eruptContextService;

    @Override
    @Transactional
    public String exec(List<Bi> data, BiReleaseModal biReleaseModal, String[] param) {
        if (eruptBiProp.getSuperAdminPublish() && !eruptUserService.getSimpleUserInfo().isSuperAdmin()) {
            throw new EruptWebApiRuntimeException("报表发布请联系 '超级管理员' 操作！");
        }
        Bi bi = data.get(0);
        Erupts.requireNull(eruptDao.queryEntity(EruptMenu.class, String.format("code = '%s'", bi.getCode())),
                "菜单已存在请勿重复发布");
        Integer max = (Integer) eruptDao.getEntityManager()
                .createQuery("select max(sort) from " + EruptMenu.class.getSimpleName()).getSingleResult();
        EruptMenu eruptMenu = new EruptMenu(bi.getCode(), biReleaseModal.getName(), BiConst.MENU_TYPE,
                bi.getCode(), MenuStatus.OPEN.getValue(), max + 10, null, biReleaseModal.getEruptMenu());
        eruptDao.persist(eruptMenu);
        //在既定角色中也保存一份
        {
            String biRoleName = "bi_view_role@auto";
            EruptRole eruptRole = new EruptRole();
            eruptRole.setCode(biRoleName);
            eruptRole.setName("报表查询角色");
            eruptRole.setStatus(true);
            eruptRole.setSort(20);
            eruptRole.setCreateTime(new Date());
            eruptRole.setUpdateTime(new Date());
            EruptRole biRole = eruptDao.persistIfNotExist(EruptRole.class, eruptRole, "code", biRoleName);
            if (null == biRole.getMenus()) {
                biRole.setMenus(new HashSet<>());
            }
            biRole.setUpdateTime(new Date());
            biRole.getMenus().add(eruptMenu);
            eruptDao.persist(biRole);
        }
        //刷新当前用户菜单
        eruptUserService.cacheUserInfo(eruptUserService.getCurrentEruptUser(), eruptContextService.getCurrentToken());
        return null;
    }
}
