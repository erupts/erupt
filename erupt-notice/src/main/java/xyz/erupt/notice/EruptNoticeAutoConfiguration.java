package xyz.erupt.notice;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.notice.modal.NoticeLog;
import xyz.erupt.notice.modal.NoticeLogDetail;
import xyz.erupt.notice.modal.NoticeScene;
import xyz.erupt.upms.prop.EruptAppProp;

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
public class EruptNoticeAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptNoticeAutoConfiguration.class);
    }

    public static final String ERUPT_NOTICE = "erupt-notice";

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_NOTICE, true);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_NOTICE).build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu notice = MetaMenu.createRootMenu("$notice", "Notice", "fa fa-bell-o", 90);
        menus.add(notice);
        menus.add(MetaMenu.createEruptClassMenu(NoticeScene.class, notice, 5, MenuTypeEnum.TREE));
        menus.add(MetaMenu.createEruptClassMenu(NoticeLog.class, notice, 10));
        menus.add(MetaMenu.createEruptClassMenu(NoticeLogDetail.class, notice, 20));
        return menus;
    }

}
