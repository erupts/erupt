package xyz.erupt.webscoket;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.webscoket.constant.EruptWebsocketConst;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@ComponentScan
@EnableWebSocket
@EnableConfigurationProperties
public class EruptWebSocketAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptWebSocketAutoConfiguration.class);
    }

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(EruptWebsocketConst.ERUPT_WEBSOCKET, true);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(EruptWebsocketConst.ERUPT_WEBSOCKET).build();
    }

}
