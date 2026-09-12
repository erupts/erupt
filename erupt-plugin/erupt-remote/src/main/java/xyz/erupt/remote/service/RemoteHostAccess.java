package xyz.erupt.remote.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

/**
 * Who may reach which host.
 * <p>
 * The menu permission answers whether a user works with remote hosts at all; this answers which
 * ones, and it is the whole answer: a host reaches exactly the users named on it, and a host that
 * names nobody is an administrators-only host. A super admin is never filtered.
 * <p>
 * Both the table and the ticket endpoint ask here: filtering the list alone would leave the host
 * one guessed id away.
 *
 * @author YuePeng
 */
@Service
public class RemoteHostAccess {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    /**
     * Query fragment for the host table, or null when the caller sees everything. The alias is the
     * entity name the erupt query builder uses.
     *
     * @param alias entity alias of the running query
     */
    public String rowFilter(String alias) {
        MetaUserinfo user = eruptUserService.getSimpleUserInfo();
        if (null == user) return alias + ".id is null";
        if (user.isSuperAdmin()) return null;
        // an id no user can carry, so a session without one matches nothing rather than everything
        Long uid = user.getId();
        return alias + ".id in (select ah.id from RemoteHost ah join ah.authUsers au where au.id = "
                + (null == uid ? -1L : uid) + ")";
    }

    /**
     * Whether the current user may open a session towards this host. Read straight from the join
     * table, so a host whose authorization changed after a page was loaded is judged on its
     * current state at the next connect.
     */
    public boolean canOpen(Long hostId) {
        MetaUserinfo user = eruptUserService.getSimpleUserInfo();
        if (null == user) return false;
        if (user.isSuperAdmin()) return true;
        List<Long> authorized = eruptDao.getEntityManager()
                .createQuery("select u.id from RemoteHost h join h.authUsers u where h.id = :id", Long.class)
                .setParameter("id", hostId).getResultList();
        return authorized.contains(user.getId());
    }

}
