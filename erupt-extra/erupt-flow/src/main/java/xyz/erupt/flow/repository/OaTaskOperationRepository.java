package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.flow.bean.entity.OaTaskOperation;

import java.util.List;

public interface OaTaskOperationRepository extends JpaRepository<OaTaskOperation, Long> {

    List<OaTaskOperation> findAllByOperatorOrderByOperationDateDesc(String account);
}
