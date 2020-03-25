package xyz.erupt.tpl.controller;


import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.service.TplService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
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

    private static final String TPL = "tpl";

    @GetMapping(value = "/{name}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getEruptFieldHtml(@PathVariable("name") String fileName, HttpServletResponse response) throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/" + TPL + "/" + fileName);
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
                        freeMarkerEngine.getTemplate(TPL + "/" + fileName).process(data, response.getWriter());
                        break;
                    case Thymeleaf:
                        Context ctx = new Context();
                        ctx.setVariables(data);
                        response.getWriter().write(thymeleafEngine.process(TPL + "/" + fileName, ctx));
                        break;
                }
            } else {
                response.getWriter().write(StreamUtils.copyToString(inputStream, Charset.forName("utf-8")));
            }
        } else {
            response.getWriter().write(StreamUtils.copyToString(inputStream, Charset.forName("utf-8")));
        }
    }

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getEruptFieldHtml(@PathVariable("erupt") String eruptName,
                                  @PathVariable("field") String field,
                                  HttpServletResponse response) throws IOException, TemplateException {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        response.setCharacterEncoding("utf-8");
        Tpl tpl = eruptModel.getEruptFieldMap().get(field).getEruptField().edit().tplType();
        if (tpl.tplHandler().isInterface()) {
            response.getWriter().write(StreamUtils
                    .copyToString(this.getClass().getResourceAsStream(tpl.path()), Charset.forName("utf-8")));
        } else {
            Map<String, Object> data = EruptSpringUtil.getBean(tpl.tplHandler()).tplAction(tpl.params());
            switch (tpl.engine()) {
                case FreeMarker:
                    freeMarkerEngine.getTemplate(tpl.path()).process(data, response.getWriter());
                    break;
                case Thymeleaf:
                    Context ctx = new Context();
                    ctx.setVariables(data);
                    response.getWriter().write(thymeleafEngine.process(tpl.path(), ctx));
                    break;
            }
        }
    }
}
