package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;

public interface OaTaskUserLinkRepository extends JpaRepository<OaTaskUserLink, Long> {

    long countByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);
}
