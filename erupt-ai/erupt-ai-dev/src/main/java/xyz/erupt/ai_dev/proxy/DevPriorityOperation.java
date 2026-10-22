package xyz.erupt.ai_dev.proxy;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.ai_dev.model.DevPriorityForm;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * Row operation that moves a requirement up or down the queue, whatever its stage: the definition stays
 * frozen while agents work from it, but its priority is only how soon it is picked.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Component
public class DevPriorityOperation implements OperationHandler<DevRequirement, DevPriorityForm> {

    @Resource
    private DevAccessService devAccessService;

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public String exec(List<DevRequirement> data, DevPriorityForm form, String[] param) {
        for (DevRequirement row : data) {
            DevRequirement r = eruptDao.find(DevRequirement.class, row.getId());
            devAccessService.requireMember(r.getProject().getId());
            r.setPriority(form.getPriority());
        }
        return null;
    }

}
