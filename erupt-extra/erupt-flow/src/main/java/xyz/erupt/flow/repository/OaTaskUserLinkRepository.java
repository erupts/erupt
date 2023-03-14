package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;

import java.util.Collection;
import java.util.List;

public interface OaTaskUserLinkRepository extends JpaRepository<OaTaskUserLink, Long> {

    long countByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);

    List<OaTaskUserLink> findAllByUserLinkTypeAndLinkIdIn(String type, Collection<String> linkIds);

    List<OaTaskUserLink> findAllByTaskId(Long taskId);
}
