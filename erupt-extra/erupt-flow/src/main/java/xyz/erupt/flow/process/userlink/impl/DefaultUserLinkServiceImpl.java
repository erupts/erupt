package xyz.erupt.flow.process.userlink.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.process.userlink.UserLinkService;
import xyz.erupt.flow.repository.EruptOrgRepository;
import xyz.erupt.flow.repository.EruptRoleRepository;
import xyz.erupt.flow.repository.EruptUserRepository;
import xyz.erupt.upms.model.EruptOrg;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;

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

    @Autowired
    private EruptOrgRepository eruptOrgRepository;
    @Autowired
    private EruptUserRepository eruptUserRepository;
    @Autowired
    private EruptRoleRepository eruptRoleRepository;

    /**
     * 查询组织架构树
     */
    @Override
    public List<OrgTreeVo> getOrgTree(String parentId, String keyword) {
        //先查询部门
        List<EruptOrg> orgs = null;
        if(StringUtils.isBlank(parentId)) {
            orgs = eruptOrgRepository.findByParentOrgIdIsNullOrderBySortAsc();
        }else {
            orgs = eruptOrgRepository.findByParentOrgIdOrderBySortAsc(Long.valueOf(parentId));
        }
        //全部转化并添加进去
        List<OrgTreeVo> vos = new LinkedList<>();
        vos.addAll(Optional.ofNullable(orgs).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getId()+"")
                        .name(o.getName())
                        .type("dept")
                        .build()
        ).collect(Collectors.toList()));
        return vos;
    }

    /**
     * 查询用户
     */
    @Override
    public List<OrgTreeVo> getOrgTreeUser(String parentId, String keywords) {
        //先查询部门
        List<EruptOrg> orgs = null;
        if(StringUtils.isBlank(parentId)) {
            orgs = eruptOrgRepository.findByParentOrgIdIsNullOrderBySortAsc();
        }else {
            orgs = eruptOrgRepository.findByParentOrgIdOrderBySortAsc(Long.valueOf(parentId));
        }
        //全部转化并添加进去
        List<OrgTreeVo> vos = new LinkedList<>();
        vos.addAll(Optional.ofNullable(orgs).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getId()+"")
                        .name(o.getName())
                        .type("dept")
                        .build()
        ).collect(Collectors.toList()));
        //再查询用户
        List<EruptUser> eruptUsers = null;
        if(StringUtils.isBlank(parentId)) {
            eruptUsers = eruptUserRepository.findByEruptOrgIdIsNull();
        }else {
            eruptUsers = eruptUserRepository.findByEruptOrgId(Long.valueOf(parentId));
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
        List<EruptRole> all = eruptRoleRepository.findAll(Sort.by("sort"));
        List<OrgTreeVo> vos = new LinkedList<>();
        //全部转化并添加进去
        vos.addAll(Optional.ofNullable(all).orElse(new ArrayList<>()).stream().map(
                o -> OrgTreeVo.builder()
                        .id(o.getCode())//用账号做标识
                        .name(o.getName())
                        .type("role")
                        .build()
        ).collect(Collectors.toList()));
        return vos;
    }

    /**
     * 按照层级返回部门领导
     * @param userId 当前用户id
     * @param startLevel 从多少级主管开始查，小于1则取1
     * @param endLevel 最多查询到多少级主管，0表示不限级
     * @return
     */
    @Override
    public LinkedHashMap<Integer, List<OrgTreeVo>> getLeaderMap(String userId, int startLevel, int endLevel) {
        //查询出当前用户的部门
        EruptUser eruptUser = eruptUserRepository.findByAccount(userId);
        if(eruptUser==null || eruptUser.getEruptOrg()==null) {
            throw new RuntimeException("用户"+userId+"不存在或没有部门");
        }
        LinkedHashMap<Integer, List<OrgTreeVo>> map = new LinkedHashMap<>();
        EruptOrg org = eruptUser.getEruptOrg().getParentOrg();//从当前部门的上一级部门开始
        while (true) {
            if(org==null || (endLevel>0 && startLevel>endLevel)) {
                break;
            }
            List<OrgTreeVo> leaders = this.getLeadersByDeptId(org.getId());
            map.put(startLevel++, leaders);
            org = org.getParentOrg();//这样可以
        }
        return map;
    }

    /**
     * 返回指定的领导
     * @return
     */
    private List<OrgTreeVo> getLeadersByDeptId(Long deptId) {
        List<EruptUser> users = eruptUserRepository.findByEruptOrgId(deptId);//先取本部门全部人员作为管理员
        List<OrgTreeVo> collect = Optional.ofNullable(users).orElse(new ArrayList<>()).stream().map(
                l -> OrgTreeVo.builder()
                        .id(l.getAccount())
                        .name(l.getName())
                        .build()
        ).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据角色id查询用户列表
     * @param roleIds
     * @return
     */
    @Override
    public LinkedHashSet<OrgTreeVo> getUserIdsByRoleIds(String... roleIds) {
        List<EruptUser> users = eruptUserRepository.findByRoleIds(Arrays.asList(roleIds));
        return toOrgTreeVoSet(users);
    }

    @Override
    public LinkedHashSet<OrgTreeVo> getAdminUsers() {
        List<EruptUser> users = eruptUserRepository.findByIsAdmin(true);
        return toOrgTreeVoSet(users);
    }

    public LinkedHashSet<OrgTreeVo> toOrgTreeVoSet(List<EruptUser> users) {
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
     * @param userId
     * @return
     */
    @Override
    public LinkedHashSet<String> getRoleIdsByUserId(String userId) {
        EruptUser user = eruptUserRepository.findByAccount(userId);
        if(user==null) {
            return new LinkedHashSet<>(0);
        }
        if(user.getRoles()==null || user.getRoles().size()<=0) {
            return new LinkedHashSet<>(0);
        }
        LinkedHashSet<String> roleIds = new LinkedHashSet<>();
        user.getRoles().stream().forEach(r -> roleIds.add(r.getCode()));
        return roleIds;
    }
}
