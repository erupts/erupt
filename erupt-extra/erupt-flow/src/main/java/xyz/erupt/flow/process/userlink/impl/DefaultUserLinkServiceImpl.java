package xyz.erupt.flow.process.userlink.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.process.userlink.UserLinkService;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOrg;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserByRoleView;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认的用户体系实现类
 * 默认使用erupt-upms的用户体系
 */
@Service
public class DefaultUserLinkServiceImpl implements UserLinkService {

    @Override
    public int priority() {
        return 0;//优先级为0，自己的用户体系必须比0更大
    }



    @Resource
    private EruptDao eruptDao;

    /**
     * 查询组织架构树
     */
    @Override
    public List<OrgTreeVo> getOrgTree(Long parentId, String keyword) {
        //先查询部门
        List<EruptOrg> orgs;
        if (null == parentId) {
            orgs = eruptDao.lambdaQuery(EruptOrg.class).isNull(EruptOrg::getParentOrg).orderBy(EruptOrg::getSort).list();
        } else {
            orgs = eruptDao.lambdaQuery(EruptOrg.class).addCondition("parentOrg.id = " + parentId).orderBy(EruptOrg::getSort).list();
        }
        //全部转化并添加进去
        List<OrgTreeVo> vos = Optional.ofNullable(orgs).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getId() + "")
                        .name(o.getName())
                        .type("dept")
                        .build()
        ).collect(Collectors.toCollection(LinkedList::new));
        return vos;
    }

    /**
     * 查询用户
     */
    @Override
    public List<OrgTreeVo> getOrgTreeUser(Long parentId, String keywords) {
        //先查询部门
        List<EruptOrg> orgs;
        if (null == parentId) {
            orgs = eruptDao.lambdaQuery(EruptOrg.class).isNull(EruptOrg::getParentOrg).orderBy(EruptOrg::getSort).list();
        } else {
            orgs = eruptDao.lambdaQuery(EruptOrg.class).addCondition("parentOrg.id = " + parentId).orderBy(EruptOrg::getSort).list();
        }
        //全部转化并添加进去
        List<OrgTreeVo> vos = new LinkedList<>();
        vos.addAll(Optional.ofNullable(orgs).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getId() + "")
                        .name(o.getName())
                        .type("dept")
                        .build()
        ).collect(Collectors.toList()));
        //再查询用户
        List<EruptUser> eruptUsers;
        if (null == parentId) {
            eruptUsers = eruptDao.lambdaQuery(EruptUser.class).isNull(EruptUser::getEruptOrg).list();
        } else {
            eruptUsers = eruptDao.lambdaQuery(EruptUser.class).addCondition("eruptOrg.id = " + parentId).list();
        }
        //全部转化并添加进去
        vos.addAll(Optional.ofNullable(eruptUsers).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getAccount())//用账号做标识
                        .name(o.getName())
                        .type("user")
                        .build()
        ).collect(Collectors.toList()));
        return vos;
    }

    @Override
    public List<OrgTreeVo> getRoleList(String keyword) {
        List<EruptRole> all = eruptDao.lambdaQuery(EruptRole.class).orderBy(EruptRole::getSort).list();
        //全部转化并添加进去
        return Optional.ofNullable(all).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getCode())//用账号做标识
                        .name(o.getName())
                        .type("role")
                        .build()
        ).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * 按照层级返回部门领导
     *
     * @param userId     当前用户id
     * @param startLevel 从多少级主管开始查，小于1则取1，1就是当前层级的领导
     * @param endLevel   最多查询到多少级主管，0表示不限级
     */
    @Override
    public Map<Integer, List<OrgTreeVo>> getLeaderMap(String userId, int startLevel, int endLevel) {
        //查询出当前用户的部门
        EruptUser eruptUser = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, userId).one();
        if (eruptUser == null || eruptUser.getEruptOrg() == null) {
            throw new EruptApiErrorTip("用户" + userId + "不存在或没有部门");
        }
        LinkedHashMap<Integer, List<OrgTreeVo>> map = new LinkedHashMap<>();
        EruptOrg org = eruptUser.getEruptOrg();//从当前部门开始
        int i = 1;
        while (org != null && (endLevel <= 0 || i <= endLevel)) {
            if (i >= startLevel) {
                List<OrgTreeVo> leaders = this.getLeadersByDeptId(org.getId());
                map.put(i, leaders);
            }
            i++;
            org = org.getParentOrg();//这样可以
        }
        return map;
    }

    /**
     * 返回指定的领导
     */
    private List<OrgTreeVo> getLeadersByDeptId(Long deptId) {
        //假设部门内排第一个的人是主管
        List<EruptUser> users = eruptDao.lambdaQuery(EruptUser.class).addCondition("eruptOrg.id = " + deptId).list();//先取本部门全部人员作为管理员
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>(0);
        }
        EruptUser first = Optional.of(users).orElse(new ArrayList<>()).stream()
                .findFirst().get();
        return Collections.singletonList(OrgTreeVo.builder()
                .id(first.getAccount())
                .name(first.getName())
                .build());
    }

    /**
     * 根据角色id查询用户列表
     *
     * @param roleIds 角色列表
     */
    @Override
    public Set<OrgTreeVo> getUserIdsByRoleIds(String... roleIds) {
        List<EruptRole> eruptRoles = eruptDao.lambdaQuery(EruptRole.class).in(EruptRole::getCode, Arrays.asList(roleIds)).list();
        List<EruptUserByRoleView> users = new ArrayList<>();
        for (EruptRole role : eruptRoles) {
            users.addAll(role.getUsers());
        }
        return toOrgTreeVoSet(users);
    }

    @Override
    public Set<OrgTreeVo> getAdminUsers() {
        List<EruptUserByRoleView> users = eruptDao.lambdaQuery(EruptUserByRoleView.class).eq(EruptUserByRoleView::getIsAdmin, true).list();
        return toOrgTreeVoSet(users);
    }

    public LinkedHashSet<OrgTreeVo> toOrgTreeVoSet(List<EruptUserByRoleView> users) {
        LinkedHashSet<OrgTreeVo> set = new LinkedHashSet<>();
        Optional.ofNullable(users).orElse(new ArrayList<>())
                .forEach(
                        l -> set.add(OrgTreeVo.builder()
                                .id(l.getAccount())
                                .name(l.getName())
                                .build()
                        )
                );
        return set;
    }

    /**
     * 查询账号对用的角色列表
     *
     * @param userId 登录账号
     */
    @Override
    public LinkedHashSet<String> getRoleIdsByUserId(String userId) {
        EruptUser user = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, userId).one();
        if (user == null) {
            return new LinkedHashSet<>(0);
        }
        if (user.getRoles() == null || user.getRoles().size() <= 0) {
            return new LinkedHashSet<>(0);
        }
        LinkedHashSet<String> roleIds = new LinkedHashSet<>();
        user.getRoles().forEach(r -> roleIds.add(r.getCode()));
        return roleIds;
    }
}
