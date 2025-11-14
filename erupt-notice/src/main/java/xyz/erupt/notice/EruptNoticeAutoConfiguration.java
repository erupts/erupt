package xyz.erupt.notice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/11/14 18:51
 */
@Configuration
@RestController
public class EruptNoticeAutoConfiguration implements EruptModule {

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-notice").build();
    }

    @Override
    public void run() {
        EruptModule.super.run();
    }

    @Override
    public List<MetaMenu> initMenus() {
        return EruptModule.super.initMenus();
    }

}
