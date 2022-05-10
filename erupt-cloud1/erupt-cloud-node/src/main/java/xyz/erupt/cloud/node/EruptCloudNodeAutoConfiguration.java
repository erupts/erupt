package xyz.erupt.cloud.node;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.cloud.node.config.EruptNodeProp;

/**
 * @author YuePeng
 * date 2021/12/16 00:15
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(EruptNodeProp.class)
public class EruptCloudNodeAutoConfiguration {
}
