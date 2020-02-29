package xyz.erupt.tpl.controller;


import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.config.TemplateConfig;
import xyz.erupt.tpl.service.TplService;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
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
    @Autowired
    private TplService tplService;

    @Autowired
    private TemplateEngine thymeleafEngine;

    @Autowired
    private Configuration freeMarkerEngine;

    @GetMapping(value = "/{name}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getEruptFieldHtml(@PathVariable("name") String fileName, HttpServletResponse response) throws Exception {
        ClassPathResource resource = new ClassPathResource(TemplateConfig.TPL + "/" + fileName);
        Method method = tplService.getAction(fileName);
        response.setCharacterEncoding("UTF-8");
        if (null != method) {
            Class<?> clazz = method.getDeclaringClass();
            Object obj = EruptSpringUtil.getBean(clazz);
            Map<String, Object> data = Map.class.cast(method.invoke(obj));
            if (null != data && data.size() > 0) {
                EruptTpl eruptTpl = obj.getClass().getAnnotation(EruptTpl.class);
                switch (eruptTpl.engine()) {
                    case FreeMarker:
                        freeMarkerEngine.getTemplate(fileName).process(data, response.getWriter());
                        break;
                    case Thymeleaf:
                        Context ctx = new Context();
                        ctx.setVariables(data);
                        response.getWriter().write(thymeleafEngine.process(fileName, ctx));
                        break;
                }
            } else {
                response.getWriter().write(FileUtils.readFileToString(resource.getFile()));
            }
        } else {
            response.getWriter().write(FileUtils.readFileToString(resource.getFile()));
        }
    }

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU)
    public String getEruptFieldHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        return tplService.execThymeleafTpl(eruptModel.getEruptFieldMap().get(field).getEruptField().edit().tplType());
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
