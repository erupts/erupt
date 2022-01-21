package xyz.erupt.core.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.erupt.core.util.EruptPropUtil;

/**
 * @author YuePeng
 * date 2022/1/14 22:17
 */
@Controller
public class EruptWebController {

    public static final String RANDOM = RandomStringUtils.randomAlphanumeric(8);

    @RequestMapping({"/"})
    public String index() {
        return "forward:index.html?v=" + EruptPropUtil.getEruptVersion();
    }

//    @RequestMapping({"app.js", "app.css", "app.module.js"})
//    public String forward(HttpServletRequest request) {
//        return request.getServletPath().replace("/", "") + "?_v=" + RANDOM;
//    }

}
