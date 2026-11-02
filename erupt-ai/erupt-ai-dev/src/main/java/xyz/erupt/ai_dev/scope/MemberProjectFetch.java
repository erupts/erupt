package xyz.erupt.ai_dev.scope;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * Projects the current user may pick: the ones they are a member of, every project for a super admin.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Component
public class MemberProjectFetch implements ChoiceFetchHandler<Void> {

    @Resource
    private DevAccessService devAccessService;

    @Override
    public List<VLModel> fetch(String[] params) {
        return devAccessService.visibleProjects().stream()
                .map(it -> new VLModel(it.getId().toString(), it.getName() + " (" + it.getCode() + ")")).toList();
    }

}
