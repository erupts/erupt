package xyz.erupt.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.erupt.core.util.EruptPropUtil;

/**
 * @author YuePeng
 * date 2022/1/14 22:17
 */
@Controller
public class EruptWebController {


    @RequestMapping({"/", "index", "/index.htm"})
    public String index() {
        return "index.html?_v=" + EruptPropUtil.getEruptVersion();
    }

}
