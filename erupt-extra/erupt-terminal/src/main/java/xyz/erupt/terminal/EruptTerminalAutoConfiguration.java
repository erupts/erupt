package xyz.erupt.terminal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@EnableWebSocket
public class EruptTerminalAutoConfiguration implements EruptModule {

    public static final String TERMINAL_KEY = "/terminal";

    static {
        EruptModuleInvoke.addEruptModule(EruptTerminalAutoConfiguration.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-terminal").description("Erupt server terminal").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu metaMenu = MetaMenu.createSimpleMenu("terminal", "Terminal",
                TERMINAL_KEY, null, 55, MenuTypeEnum.ROUTER.getCode());
        metaMenu.setIcon("fa fa-terminal");
        menus.add(metaMenu);
        return menus;
    }
}
