package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.erupt.upms.model.EruptUser;

import java.util.Collection;
import java.util.List;

public interface EruptUserRepository extends JpaRepository<EruptUser, Long> {

    /**
     * 查询机构为空的人员
     * @return
     */
    List<EruptUser> findByEruptOrgIdIsNull();

    /**
     * 根据上级机构id查询
     * @param orgId
     * @return
     */
    List<EruptUser> findByEruptOrgId(Long orgId);

    /**
     * 模糊匹配
     * @param keywords
     * @return
     */
    List<EruptUser> findByAccountContainingOrNameContaining(String keywords, String keyword1);

    /**
     * 根据角色id查询数据
     * @param roleIds
     * @return
     */
    @Query("from EruptUser where id in (select userId from EruptUserRole where roleId in :roleIds ) ")
    List<EruptUser> findByRoleIds(Collection<String> roleIds);

    /**
     * 查询超管用户
     * @param b
     * @return
     */
    List<EruptUser> findByIsAdmin(boolean b);

    /**
     * 根据账号查询
     * @param userId
     * @return
     */
    EruptUser findByAccount(String userId);
}
