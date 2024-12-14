package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.upms.model.EruptOrg;

import java.util.List;

public interface EruptOrgRepository extends JpaRepository<EruptOrg, Long> {

    /**
     * 查询上级为空的单位
     */
    List<EruptOrg> findByParentOrgIdIsNullOrderBySortAsc();

    /**
     * 根据上级机构id查询
     */
    List<EruptOrg> findByParentOrgIdOrderBySortAsc(Long orgId);
}
