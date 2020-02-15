package xyz.erupt.tpl.controller;


import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.annotation.sub_erupt.Html;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.service.CoreService;

import javax.servlet.http.HttpServletResponse;

import static xyz.erupt.core.constant.RestPath.ERUPT_API;

/**
 * Erupt 页面结构构建信息
 * @author liyuepeng
 * @date 2018-09-28.
 */
@RestController
@RequestMapping(ERUPT_API + "/tpl")
public class EruptTplController {

    @Autowired
    private TemplateEngine templateEngine;

    private static final String HTML = ".html";

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU)
    public String getEruptFieldHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        return execTemplate(eruptModel.getEruptFieldMap().get(field).getEruptField().edit().htmlType());
    }

    @GetMapping(value = "/html/{name}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU)
    public String getEruptHtml(@PathVariable("name") String name, HttpServletResponse response) {
        try {
            if (!name.endsWith(HTML)) {
                name += HTML;
            }
            Resource resource = new ClassPathResource("tpl/" + name);
            return FileUtils.readFileToString(resource.getFile());
        } catch (Exception e) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
    }

//    @GetMapping(value = "/html/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
//    @ResponseBody
//    @EruptRouter(authIndex = 2)
//    public String getEruptHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) throws IOException {
//        EruptModel eruptModel = CoreService.getErupt(eruptName);
//        String path = eruptModel.getErupt().beforeHtml().path();
//        Resource resource = new ClassPathResource(path);
//        return FileUtils.readFileToString(resource.getFile());
//    }

    private String execTemplate(Html html) {
        try {
            Resource resource = new ClassPathResource(html.path());
            String template = FileUtils.readFileToString(resource.getFile());
            if (!html.htmlHandler().isInterface()) {
                Context ctx = new Context();
                ctx.setVariables(html.htmlHandler().newInstance().getData(html.params()));
                return templateEngine.process(template, ctx);
            } else {
                return template;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
