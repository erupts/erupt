package xyz.erupt.flow.process.userlink.impl;

import lombok.Data;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.process.userlink.UserLinkService;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 用户体系解析
 * 这个实现其实是一个代理，他会选择优先级最高的一个实例调用
 */
@Data
public class UserLinkServiceHolder implements UserLinkService {

    private UserLinkService userLinkService;

    @Override
    public int priority() {
        return -1;//优先级非常低
    }

    @Override
    public List<OrgTreeVo> getOrgTree(String parentId, String keyword) {
        return this.userLinkService.getOrgTree(parentId, keyword);
    }

    @Override
    public List<OrgTreeVo> getOrgTreeUser(String parentId, String keyword) {
        return this.userLinkService.getOrgTreeUser(parentId, keyword);
    }

    @Override
    public List<OrgTreeVo> getRoleList(String keyword) {
        return this.userLinkService.getRoleList(keyword);
    }

    @Override
    public LinkedHashMap<Integer, List<OrgTreeVo>> getLeaderMap(String userId, int startLevel, int limitLevel) {
        return this.userLinkService.getLeaderMap(userId, startLevel, limitLevel);
    }

    @Override
    public LinkedHashSet<OrgTreeVo> getUserIdsByRoleIds(String... roleIds) {
        return this.userLinkService.getUserIdsByRoleIds(roleIds);
    }

    @Override
    public LinkedHashSet<OrgTreeVo> getAdminUsers() {
        return this.userLinkService.getAdminUsers();
    }

    @Override
    public LinkedHashSet<String> getRoleIdsByUserId(String userId) {
        return this.userLinkService.getRoleIdsByUserId(userId);
    }

}
