package xyz.erupt.notice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.modal.NoticeLogDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/11/14 18:51
 */
@Configuration
@ComponentScan
@EruptScan
@EntityScan
@EnableConfigurationProperties
public class EruptNoticeAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptNoticeAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-notice").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu notice = MetaMenu.createRootMenu("$notice", "消息通知", "fa fa-bell", 90);
        menus.add(notice);
        menus.add(MetaMenu.createEruptClassMenu(NoticeLog.class, notice, 10));
        menus.add(MetaMenu.createEruptClassMenu(NoticeLogDetail.class, notice, 20));
        return menus;
    }

}
