package xyz.erupt.flow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;
import xyz.erupt.flow.repository.OaTaskUserLinkRepository;
import xyz.erupt.flow.service.TaskUserLinkService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class TaskUserLinkServiceImpl implements TaskUserLinkService {

    @Autowired
    private OaTaskUserLinkRepository repository;

    @Override
    public long countByTaskId(Long taskId) {
        return repository.countByTaskId(taskId);
    }

    @Override
    public List<OaTaskUserLink> saveBatch(Collection<OaTaskUserLink> links) {
        return repository.saveAll(links);
    }

    @Override
    public void removeByTaskId(Long taskId) {
        repository.deleteByTaskId(taskId);
    }

    @Override
    public List<OaTaskUserLink> listByRoleIds(Collection<String> roleIds) {
        if(CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>(0);
        }
        return repository.findAllByUserLinkTypeAndLinkIdIn("ROLES", roleIds);
    }

    @Override
    public List<OaTaskUserLink> listByUserIds(Collection<String> userIds) {
        if(CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>(0);
        }
        return repository.findAllByUserLinkTypeAndLinkIdIn("USERS", userIds);
    }

    @Override
    public List<OaTaskUserLink> listByTaskId(Long taskId) {
        return repository.findAllByTaskId(taskId);
    }
}
