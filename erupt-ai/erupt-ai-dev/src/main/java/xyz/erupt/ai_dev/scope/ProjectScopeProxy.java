package xyz.erupt.ai_dev.scope;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.annotation.fun.DataProxy;

import java.util.List;

/**
 * Until the user picks, the header holds their first project, so the pool opens on something.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Component
public class ProjectScopeProxy implements DataProxy<DevProjectScope> {

    @Resource
    private DevAccessService devAccessService;

    @Override
    public void addBehavior(DevProjectScope scope) {
        List<DevProject> projects = devAccessService.visibleProjects();
        if (!projects.isEmpty()) scope.setProject(projects.get(0).getId());
    }

}
