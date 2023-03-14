package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.entity.OaTaskUserLink;

import java.util.Collection;
import java.util.List;

public interface TaskUserLinkService {

    long countByTaskId(Long taskId);

    List<OaTaskUserLink> saveBatch(Collection<OaTaskUserLink> links);

    void removeByTaskId(Long id);
}
