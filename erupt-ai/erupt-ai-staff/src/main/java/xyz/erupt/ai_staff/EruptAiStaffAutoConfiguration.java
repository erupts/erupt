package xyz.erupt.ai_staff;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_staff.model.AiStaff;
import xyz.erupt.ai_staff.model.AiStaffChannel;
import xyz.erupt.ai_staff.model.AiStaffTask;
import xyz.erupt.ai_staff.model.AiStaffTaskLog;
import xyz.erupt.ai_staff.service.AiStaffScheduler;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2026/8/3
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
@Slf4j
public class EruptAiStaffAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiStaffAutoConfiguration.class);
    }

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiStaffScheduler aiStaffScheduler;

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-staff").description("Digital AI employees — autonomous staff with duties, schedules and work reports").build();
    }

    @Override
    public void run() {
        for (AiStaffTask task : eruptDao.lambdaQuery(AiStaffTask.class).eq(AiStaffTask::getEnable, true).list()) {
            try {
                aiStaffScheduler.refresh(task);
            } catch (Exception e) {
                log.warn("The AI staff task named '{}' failed to be scheduled: {}", task.getName(), e.getMessage());
            }
        }
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$ai-staff", "AI Staff", "fa fa-user-circle", 26));
        menus.add(MetaMenu.createEruptClassMenu(AiStaff.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(AiStaffChannel.class, menus.get(0), 20));
        menus.add(MetaMenu.createEruptClassMenu(AiStaffTask.class, menus.get(0), 30, MenuStatus.HIDE));
        menus.add(MetaMenu.createEruptClassMenu(AiStaffTaskLog.class, menus.get(0), 40, MenuStatus.HIDE));
        return menus;
    }

}
