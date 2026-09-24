package xyz.erupt.webscoket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Slf4j
@Configuration
@ComponentScan
@EnableWebSocket
@EnableConfigurationProperties
public class EruptWebSocketAutoConfiguration implements EruptModule {

    public static final String ERUPT_WEBSOCKET = "erupt-websocket";

    static {
        EruptModuleInvoke.addEruptModule(EruptWebSocketAutoConfiguration.class);
    }

    @Resource
    private EruptAppProp eruptAppProp;

    @PostConstruct
    public void post() {
        eruptAppProp.registerProp(ERUPT_WEBSOCKET, true);
    }

    /**
     * Registers the annotated endpoints with the servlet container's websocket support. A
     * context without one, such as a {@code @SpringBootTest} in its default mock environment,
     * has nothing to register into, and an application that only wanted its beans must not
     * fail to start over that: the endpoints are simply left out until a real container is there.
     */
    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter() {
            @Override
            public void afterPropertiesSet() {
                if (null != getServerContainer()) super.afterPropertiesSet();
            }

            @Override
            public void afterSingletonsInstantiated() {
                if (null == getServerContainer()) {
                    log.warn("no websocket ServerContainer in this servlet context, websocket endpoints are not registered");
                    return;
                }
                super.afterSingletonsInstantiated();
            }
        };
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name(ERUPT_WEBSOCKET).build();
    }

}
