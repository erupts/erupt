package xyz.erupt.ai_dev.scope;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * Repositories of the project in the page header, by alias.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Component
public class ProjectRepoFetch implements ChoiceFetchHandler<Void> {

    @Resource
    private DevAccessService devAccessService;

    @Override
    public List<VLModel> fetch(String[] params) {
        return devAccessService.currentProject().getRepos().stream().map(DevProjectRepo::getAlias)
                .sorted().map(alias -> new VLModel(alias, alias)).toList();
    }

}
