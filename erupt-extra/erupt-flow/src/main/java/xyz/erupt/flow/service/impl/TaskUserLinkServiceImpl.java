package xyz.erupt.flow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;
import xyz.erupt.flow.repository.OaTaskUserLinkRepository;
import xyz.erupt.flow.service.TaskUserLinkService;

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
}
