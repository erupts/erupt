package xyz.erupt.remote;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.remote.model.RemoteHost;

import java.util.ArrayList;
import java.util.List;

/**
 * Remote desktop module: manages remote hosts and bridges VNC sessions to the browser.
 *
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@EnableWebSocket
public class EruptRemoteAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptRemoteAutoConfiguration.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-remote").description("Erupt remote desktop").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu root = MetaMenu.createRootMenu("$remote", "Remote Desktop", "fa fa-display", 56);
        menus.add(root);
        menus.add(MetaMenu.createEruptClassMenu(RemoteHost.class, root, 10));
        return menus;
    }
}
