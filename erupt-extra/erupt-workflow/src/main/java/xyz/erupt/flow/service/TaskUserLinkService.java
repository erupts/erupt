package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.entity.OaTaskUserLink;

import java.util.Collection;
import java.util.List;

public interface TaskUserLinkService {

    long countByTaskId(Long taskId);

    boolean saveBatch(Collection<OaTaskUserLink> links);

    List<OaTaskUserLink> listByRoleIds(Collection<String> roleIds);

    List<OaTaskUserLink> listByUserIds(Collection<String> roleIds);

    List<OaTaskUserLink> listByTaskId(Long taskId);

    /**
     * 统计任务的处理人
     * @param id
     * @return
     */
    int countUsersByTaskId(Long id);
}
