package xyz.erupt.ldap;

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
public class EruptLdapAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptLdapAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-data-ldap").description("LDAP directory data source implement").build();
    }

}
