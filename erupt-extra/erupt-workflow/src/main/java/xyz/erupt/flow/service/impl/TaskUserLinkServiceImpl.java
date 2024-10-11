package xyz.erupt.flow.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.TaskUserLinkService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskUserLinkServiceImpl implements TaskUserLinkService {

    @Resource
    private UserLinkServiceHolder userLinkServiceHolder;

    @Resource
    private EruptDao eruptDao;

    @Override
    public long countByTaskId(Long taskId) {
        return eruptDao.lambdaQuery(OaTaskUserLink.class).eq(OaTaskUserLink::getTaskId, taskId).count();
    }

    @Override
    public List<OaTaskUserLink> listByRoleIds(Collection<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>(0);
        }
        return eruptDao.lambdaQuery(OaTaskUserLink.class).eq(OaTaskUserLink::getUserLinkType, "ROLES")
                .in(OaTaskUserLink::getLinkId, roleIds).list();
    }

    @Override
    public List<OaTaskUserLink> listByUserIds(Collection<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>(0);
        }
        return eruptDao.lambdaQuery(OaTaskUserLink.class)
                .eq(OaTaskUserLink::getUserLinkType, "USERS")
                .in(OaTaskUserLink::getLinkId, userIds).list();
    }

    @Override
    public List<OaTaskUserLink> listByTaskId(Long taskId) {
        return eruptDao.lambdaQuery(OaTaskUserLink.class).eq(OaTaskUserLink::getTaskId, taskId).list();
    }

    /**
     * 统计任务的实际处理人数量
     * 即把 角色 转化为人进行统计
     *
     * @param taskId 任务 ID
     * @return 用户数量
     */
    @Override
    public int countUsersByTaskId(Long taskId) {
        List<OaTaskUserLink> links = this.listByTaskId(taskId);
        if (CollectionUtils.isEmpty(links)) {
            return 0;
        }
        //查询用户数
        Set<String> userIds = links.stream()
                .filter(link -> FlowConstant.USER_LINK_USERS.equals(link.getUserLinkType()))
                .map(OaTaskUserLink::getLinkId)
                .collect(Collectors.toSet());
        //角色ids
        Set<String> roleIds = links.stream()
                .filter(link -> FlowConstant.USER_LINK_ROLES.equals(link.getUserLinkType()))
                .map(OaTaskUserLink::getLinkId)
                .collect(Collectors.toSet());
        //如果有角色，把角色解析为用户数
        if (!CollectionUtils.isEmpty(roleIds)) {
            Set<OrgTreeVo> users = userLinkServiceHolder.getUserIdsByRoleIds(roleIds.toArray(new String[0]));
            userIds.addAll(users.stream().map(OrgTreeVo::getId).collect(Collectors.toSet()));
        }
        return userIds.size();
    }

    @Override
    public boolean saveBatch(Collection<OaTaskUserLink> entityList) {
        entityList.forEach(it -> eruptDao.persist(entityList));
        return true;
    }
}
