package xyz.erupt.flow.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.flow.process.userlink.UserLinkService;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;

import java.util.Arrays;

/**
 * 配置用户体系的代理
 */
@Configuration
public class UserLinkServiceHolderConfiguration {

    /**
     * 注入所有的 userLinkServices，然后按照优先级选择其中一个
     * @param userLinkServices
     */
    @Bean
    public UserLinkServiceHolder userLinkServiceHolder(UserLinkService... userLinkServices) {
        UserLinkServiceHolder holder = new UserLinkServiceHolder();
        //获取到优先级大于等于0的实例中，优先级最高的一个
        UserLinkService userLinkService = Arrays.stream(userLinkServices)
                .filter(e -> e.priority() >= 0)
                .sorted()
                .findFirst()
                .get();
        if(userLinkService==null) {
            throw new RuntimeException("至少要有一个 " +UserLinkService.class.getName()+" 的实例");
        }
        holder.setUserLinkService(userLinkService);
        return holder;
    }
}
