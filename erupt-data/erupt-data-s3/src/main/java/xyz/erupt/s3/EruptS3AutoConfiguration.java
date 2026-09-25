package xyz.erupt.s3;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.s3.prop.EruptS3Properties;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(EruptS3Properties.class)
public class EruptS3AutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptS3AutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-s3").description("S3-compatible object storage data source implement").build();
    }

}
