package xyz.erupt.tpl.controller;


import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.service.TplService;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static xyz.erupt.core.constant.RestPath.ERUPT_API;

/**
 * Erupt 页面结构构建信息
 *
 * @author liyuepeng
 * @date 2018-09-28.
 */
@RestController
@RequestMapping(ERUPT_API + "/tpl")
public class EruptTplController {

    private static final String TPL = "tpl";
    @Autowired
    private TplService tplService;

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private Configuration freemarkerConfig;

    private static final String HTML = ".html";

    @GetMapping(value = "/{name}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getEruptFieldHtml(@PathVariable("name") String name, HttpServletResponse response) throws Exception {
        String fileName = name;
        if (name.endsWith(HTML)) {
            name = name.replace(HTML, "");
        } else {
            fileName = name + HTML;
        }
        Resource resource = new ClassPathResource(TPL + "/" + fileName);
        String tpl = FileUtils.readFileToString(resource.getFile());
        Class clazz = tplService.getAction(name);
        response.setCharacterEncoding("UTF-8");
        if (null != clazz) {
            Object obj = EruptSpringUtil.getBean(clazz);
            Map<String, Object> data = (Map<String, Object>) clazz.getMethod(name).invoke(obj);
            if (null != data && data.size() > 0) {
                EruptTpl eruptTpl = obj.getClass().getAnnotation(EruptTpl.class);
                switch (eruptTpl.engine()) {
                    case FreeMarker:
                        freemarkerConfig.setDefaultEncoding("utf-8");
                        freemarkerConfig.setDirectoryForTemplateLoading(new ClassPathResource(TPL).getFile());
                        Template template = freemarkerConfig.getTemplate(fileName);
                        template.process(data, response.getWriter());
                        response.setCharacterEncoding("utf-8");
                        break;
                    case Thymeleaf:
                        Context ctx = new Context();
                        ctx.setVariables(data);
                        response.getWriter().write(templateEngine.process(tpl, ctx));
                }
            } else {
                response.getWriter().write(tpl);
            }
        } else {
            response.getWriter().write(tpl);
        }
    }

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU)
    public String getEruptFieldHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        return tplService.execThymeleafTpl(eruptModel.getEruptFieldMap().get(field).getEruptField().edit().htmlType());
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
