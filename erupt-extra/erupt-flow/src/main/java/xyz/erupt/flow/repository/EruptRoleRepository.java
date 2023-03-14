package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.upms.model.EruptRole;

public interface EruptRoleRepository extends JpaRepository<EruptRole, Long> {
}
