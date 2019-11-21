package xyz.erupt.tpl.controller;


import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.annotation.sub_erupt.Html;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.CoreService;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_BUILD)
public class EruptTplController {


    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
//    @EruptRouter(authIndex = 2)
    public String getEruptFieldHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        return execTemplate(eruptModel.getEruptFieldMap().get(field).getEruptField().edit().htmlType());
    }


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

//    @GetMapping(value = "/html/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
//    @ResponseBody
//    @EruptRouter(authIndex = 2)
//    public String getEruptHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) throws IOException {
//        EruptModel eruptModel = CoreService.getErupt(eruptName);
//        String path = eruptModel.getErupt().beforeHtml().path();
//        Resource resource = new ClassPathResource(path);
//        return FileUtils.readFileToString(resource.getFile());
//    }
}
