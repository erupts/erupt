package xyz.erupt.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@Controller
public class EruptWebConfig {

    @GetMapping("/")
    public String index(HttpServletResponse response) {
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        return "forward:index.html?v=" + this.getClass().hashCode();
    }

}
