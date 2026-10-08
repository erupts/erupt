package xyz.erupt.ai_tune;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.TuneEvent;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.ai_tune.model.TuneSample;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2026/10/8
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class EruptAiTuneAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiTuneAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-tune")
                .description("Model fine-tuning — datasets, training jobs, loss curves, checkpoints and tuned-model registration").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$tune", "Fine-tuning", "fa fa-sliders", 28));
        menus.add(MetaMenu.createEruptClassMenu(TuneDataset.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(TuneJob.class, menus.get(0), 20));
        menus.add(MetaMenu.createEruptClassMenu(TuneSample.class, menus.get(0), 30, MenuStatus.HIDE));
        menus.add(MetaMenu.createEruptClassMenu(TuneEvent.class, menus.get(0), 40, MenuStatus.HIDE));
        return menus;
    }

}
