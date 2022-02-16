package xyz.erupt.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author YuePeng
 * date 2022/1/14 22:17
 */
@Controller
public class EruptWebController {

    @RequestMapping("/")
    public String index() {
        return "forward:index.html?v=" + EruptWebController.class.hashCode();
    }


    @RequestMapping("/home.$.html")
    public String home() {
        return "forward:home.html?v=" + EruptWebController.class.hashCode();
    }

}
