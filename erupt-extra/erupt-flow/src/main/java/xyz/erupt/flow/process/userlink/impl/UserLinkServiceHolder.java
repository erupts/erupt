package xyz.erupt.flow.process.userlink.impl;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.process.userlink.UserLinkService;

import java.util.*;

/**
 * 用户体系解析
 * 这个实现其实是一个代理，他会选择优先级最高的一个实例调用
 */
@Getter
@Setter
public class UserLinkServiceHolder implements UserLinkService {

    private UserLinkService userLinkService;

    @Override
    public int priority() {
        return -1;//优先级非常低
    }

    @Override
    public List<OrgTreeVo> getOrgTree(Long parentId, String keyword) {
        return this.userLinkService.getOrgTree(parentId, keyword);
    }

    @Override
    public List<OrgTreeVo> getOrgTreeUser(Long parentId, String keyword) {
        return this.userLinkService.getOrgTreeUser(parentId, keyword);
    }

    @Override
    public List<OrgTreeVo> getRoleList(String keyword) {
        return this.userLinkService.getRoleList(keyword);
    }

    @Override
    public Map<Integer, List<OrgTreeVo>> getLeaderMap(String userId, int startLevel, int limitLevel) {
        return this.userLinkService.getLeaderMap(userId, startLevel, limitLevel);
    }

    @Override
    public Set<OrgTreeVo> getUserIdsByRoleIds(String... roleIds) {
        return this.userLinkService.getUserIdsByRoleIds(roleIds);
    }

    @Override
    public Set<OrgTreeVo> getAdminUsers() {
        return this.userLinkService.getAdminUsers();
    }

    @Override
    public Set<String> getRoleIdsByUserId(String userId) {
        return this.userLinkService.getRoleIdsByUserId(userId);
    }

}
