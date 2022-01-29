package xyz.erupt.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.erupt.core.util.EruptInformation;

/**
 * @author YuePeng
 * date 2022/1/14 22:17
 */
@Controller
public class EruptWebController {

    @RequestMapping({"/"})
    public String index() {
        return "forward:index.html?v=" + EruptInformation.getEruptVersion();
    }

}
