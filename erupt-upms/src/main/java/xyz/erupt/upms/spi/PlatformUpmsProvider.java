package xyz.erupt.upms.spi;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.erupt.core.view.KV;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The built-in provider: platform sessions (tenant id {@code null}) resolve against the erupt-upms
 * tables. It is ordered last so any other registered provider may claim a tenant — or the platform
 * — first.
 *
 * @author YuePeng
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PlatformUpmsProvider implements UpmsProvider {

    @Resource
    private EruptDao eruptDao;

    @Override
    public boolean supports(String tenantId) {
        return null == tenantId;
    }

    @Override
    public UpmsUser findUser(String tenantId, Long userId) {
        EruptUserVo user = eruptDao.find(EruptUserVo.class, userId);
        return null == user ? null : toUpmsUser(user);
    }

    @Override
    public Map<Long, UpmsUser> findUsers(String tenantId, Collection<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return eruptDao.lambdaQuery(EruptUserVo.class).in(EruptUserVo::getId, userIds).list()
                .stream().collect(Collectors.toMap(EruptUserVo::getId, PlatformUpmsProvider::toUpmsUser, (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public UpmsUserDetail userDetail(String tenantId, Long userId) {
        EruptUser user = eruptDao.find(EruptUser.class, userId);
        if (null == user) return null;
        UpmsUserDetail detail = new UpmsUserDetail();
        detail.setId(user.getId());
        detail.setName(user.getName());
        detail.setAvatar(user.getAvatar());
        detail.setStatus(user.getStatus());
        Optional.ofNullable(user.getEruptOrg()).ifPresent(org -> detail.setOrgId(org.getId()));
        Optional.ofNullable(user.getEruptPost()).ifPresent(post -> detail.setPostId(post.getId()));
        detail.setRoleIds(null == user.getRoles() ? Set.of() : user.getRoles().stream().map(EruptRole::getId).collect(Collectors.toSet()));
        detail.setOrgHead(null != user.getHeadOrg() && !user.getHeadOrg().isEmpty());
        return detail;
    }

    @Override
    public UpmsUserContact findContact(String tenantId, Long userId) {
        EruptUser user = eruptDao.find(EruptUser.class, userId);
        return null == user ? null : toContact(user);
    }

    @Override
    public Map<Long, UpmsUserContact> findContacts(String tenantId, Collection<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return eruptDao.lambdaQuery(EruptUser.class).in(EruptUser::getId, userIds).list()
                .stream().collect(Collectors.toMap(EruptUser::getId, PlatformUpmsProvider::toContact, (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public List<UpmsUser> users(String tenantId) {
        return eruptDao.lambdaQuery(EruptUserVo.class).list().stream().map(PlatformUpmsProvider::toUpmsUser).toList();
    }

    @Override
    public List<UpmsUser> usersByOrg(String tenantId, Long orgId) {
        if (null == orgId) return this.users(tenantId);
        return eruptDao.lambdaQuery(EruptUser.class)
                .with(EruptUser::getEruptOrg).eq(EruptOrg::getId, orgId)
                .list().stream().map(user -> new UpmsUser(user.getId(), user.getName(), user.getAvatar(), user.getStatus())).toList();
    }

    @Override
    public List<KV<Long, String>> roles(String tenantId) {
        return eruptDao.lambdaQuery(EruptRole.class).list().stream().map(role -> new KV<>(role.getId(), role.getName())).toList();
    }

    @Override
    public List<KV<Long, String>> orgs(String tenantId) {
        return eruptDao.lambdaQuery(EruptOrg.class).orderBy(EruptOrg::getSort).list().stream().map(org -> new KV<>(org.getId(), org.getName())).toList();
    }

    @Override
    public List<KV<Long, String>> posts(String tenantId) {
        return eruptDao.lambdaQuery(EruptPost.class).orderBy(EruptPost::getWeight).list().stream().map(post -> new KV<>(post.getId(), post.getName())).toList();
    }

    @Override
    public List<Long> userIdsByRole(String tenantId, Long roleId) {
        EruptRole role = eruptDao.find(EruptRole.class, roleId);
        if (null == role || null == role.getUsers()) return List.of();
        return role.getUsers().stream().map(EruptUserByRoleView::getId).toList();
    }

    @Override
    public List<Long> userIdsByOrg(String tenantId, Long orgId) {
        return eruptDao.lambdaQuery(EruptUser.class)
                .with(EruptUser::getEruptOrg).eq(EruptOrg::getId, orgId).with()
                .listSelect(EruptUser::getId);
    }

    @Override
    public List<Long> userIdsByPost(String tenantId, Long postId) {
        return eruptDao.lambdaQuery(EruptUser.class)
                .with(EruptUser::getEruptPost).eq(EruptPost::getId, postId).with()
                .listSelect(EruptUser::getId);
    }

    @Override
    public List<Long> orgHeads(String tenantId, Long userId, int level) {
        return this.usersByOrgLevel(userId, level, "headOrg");
    }

    @Override
    public List<Long> divisionLeaders(String tenantId, Long userId, int level) {
        return this.usersByOrgLevel(userId, level, "divisionOrg");
    }

    @Override
    public Long adminUserId(String tenantId) {
        EruptUser admin = eruptDao.lambdaQuery(EruptUser.class)
                .eq(EruptUser::getIsAdmin, true)
                .orderByDesc(EruptUser::getId)
                .limit(1).one();
        return null == admin ? null : admin.getId();
    }

    private List<Long> usersByOrgLevel(Long userId, int level, String orgField) {
        List<Long> users = new ArrayList<>();
        EruptUser user = eruptDao.find(EruptUser.class, userId);
        if (null == user || null == user.getEruptOrg()) return users;
        EruptOrg org = user.getEruptOrg();
        // walk up `level` levels of parent organisations
        for (int i = 0; i < level; i++) {
            if (null == org.getParentOrg()) {
                log.warn("The organization hierarchy is not enough");
                return users;
            }
            org = org.getParentOrg();
        }
        List<EruptUser> heads = eruptDao.getEntityManager()
                .createQuery("select u from EruptUser u join u." + orgField + " h where h = :orgId", EruptUser.class)
                .setParameter("orgId", org.getId())
                .getResultList();
        for (EruptUser head : heads) {
            users.add(head.getId());
        }
        if (heads.isEmpty()) {
            log.warn("The organization {} has no head user", org.getName());
        }
        return users;
    }

    private static UpmsUserContact toContact(EruptUser user) {
        UpmsUserContact contact = new UpmsUserContact(null, user.getId(), user.getAccount(), user.getName());
        contact.setAvatar(user.getAvatar());
        contact.setStatus(user.getStatus());
        contact.setEmail(user.getEmail());
        contact.setPhone(user.getPhone());
        return contact;
    }

    private static UpmsUser toUpmsUser(EruptUserVo user) {
        return new UpmsUser(user.getId(), user.getName(), user.getAvatar(), user.getStatus());
    }

}
