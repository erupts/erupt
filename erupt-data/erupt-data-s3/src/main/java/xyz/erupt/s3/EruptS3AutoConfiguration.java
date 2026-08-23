package xyz.erupt.s3;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.ModuleInfo;

/**
 * @author YuePeng
 */
@Configuration
@ComponentScan
public class EruptS3AutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptS3AutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-s3").description("S3-compatible object storage data source implement").build();
    }

}
