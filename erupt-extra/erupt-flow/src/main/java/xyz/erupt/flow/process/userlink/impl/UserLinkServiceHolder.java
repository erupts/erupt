package xyz.erupt.flow.process.userlink.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.process.userlink.UserLinkService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 用户体系解析
 * 这个实现其实是一个代理，他会选择优先级最高的一个实例调用
 */
@Service
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

    /**
     * 注入所有的 userLinkServices，然后按照优先级选择其中一个
     * @param userLinkServices
     */
    @Autowired
    public UserLinkServiceHolder(UserLinkService... userLinkServices) {
        //获取到优先级大于等于0的实例中，优先级最高的一个
        this.userLinkService = Arrays.stream(userLinkServices).filter(e -> e.priority()>=0).sorted().findFirst().get();
        if(this.userLinkService==null) {
            throw new RuntimeException("至少要有一个 " +UserLinkService.class.getName()+" 的实例");
        }
    }

}
