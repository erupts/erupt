package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.erupt.upms.model.EruptUser;

import java.util.Collection;
import java.util.List;

public interface EruptUserRepository extends JpaRepository<EruptUser, Long> {

    /**
     * 查询机构为空的人员
     */
    List<EruptUser> findByEruptOrgIdIsNull();

    /**
     * 根据上级机构id查询
     */
    List<EruptUser> findByEruptOrgId(Long orgId);

    /**
     * 模糊匹配
     */
    List<EruptUser> findByAccountContainingOrNameContaining(String keywords, String keyword1);

    /**
     * 根据角色id查询数据
     */
    @Query("from EruptUser where id in (select userId from EruptUserRole where roleId in (select id from EruptRole where code in :roleIds) ) ")
    List<EruptUser> findByRoleIds(Collection<String> roleIds);

    /**
     * 查询超管用户
     */
    List<EruptUser> findByIsAdmin(boolean b);

    /**
     * 根据账号查询
     */
    EruptUser findByAccount(String userId);

    /**
     * 根据账号集合查询
     */
    List<EruptUser> findByAccountIn(Collection<String> accounts);
}
