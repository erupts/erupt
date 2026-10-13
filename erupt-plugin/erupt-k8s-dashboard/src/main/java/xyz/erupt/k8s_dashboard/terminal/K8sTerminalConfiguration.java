package xyz.erupt.k8s_dashboard.terminal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import xyz.erupt.upms.service.EruptTokenService;

/**
 * Registers the terminal socket when the application ships WebSocket support (spring-boot-starter-websocket)
 * and erupt-upms sessions; without them the rest of the module works and the terminal stays off.
 *
 * @author YuePeng
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ServerEndpointExporter.class, EruptTokenService.class})
public class K8sTerminalConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter() {
        // The classes being present does not mean the container is: a mock servlet context
        // (tests, some embedded setups) has no ServerContainer, so skip instead of failing startup
        return new ServerEndpointExporter() {
            @Override
            public void afterPropertiesSet() {
                if (null != getServerContainer()) super.afterPropertiesSet();
            }

            @Override
            public void afterSingletonsInstantiated() {
                if (null != getServerContainer()) super.afterSingletonsInstantiated();
            }
        };
    }

    @Bean
    public K8sTerminalEndpoint k8sTerminalEndpoint() {
        return new K8sTerminalEndpoint();
    }

}
