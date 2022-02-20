package xyz.erupt.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@Controller
public class EruptWebConfig {

    @RequestMapping("/")
    public String index() {
        return "forward:index.html?v=" + EruptWebConfig.class.hashCode();
    }

}
