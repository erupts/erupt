package xyz.erupt.tpl.controller;


import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.service.EruptTplService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
    private EruptTplService tplService;

    @Autowired
    private TemplateEngine thymeleafEngine;

    @Autowired
    private Configuration freeMarkerEngine;

    @Autowired
    private EruptTplService eruptTplService;

    private static final String TPL = "tpl";

    @GetMapping(value = "/{name}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getEruptFieldHtml(@PathVariable("name") String fileName, HttpServletResponse response) throws Exception {
        @Cleanup InputStream inputStream = this.getClass().getResourceAsStream("/" + TPL + "/" + fileName);
        Method method = tplService.getAction(fileName);
        response.setCharacterEncoding("UTF-8");
        if (null != method) {
            Class<?> clazz = method.getDeclaringClass();
            Object obj = EruptSpringUtil.getBean(clazz);
            Map<String, Object> data = (Map) method.invoke(obj);
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
                response.getWriter().write(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
            }
        } else {
            response.getWriter().write(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        }
    }

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getEruptFieldHtml(@PathVariable("erupt") String eruptName,
                                  @PathVariable("field") String field,
                                  HttpServletResponse response) throws IOException, TemplateException {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        response.setCharacterEncoding("utf-8");
        Tpl tpl = eruptModel.getEruptFieldMap().get(field).getEruptField().edit().tplType();
        eruptTplService.tplToResponse(tpl, response);
    }


    @GetMapping(value = "/operation_tpl/{erupt}/{code}", produces = {"text/html;charset=utf-8"})
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void getOperationTpl(@PathVariable("erupt") String eruptName,
                                @PathVariable("code") String code,
                                HttpServletResponse response) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        for (RowOperation operation : eruptModel.getErupt().rowOperation()) {
            if (operation.code().equals(code)) {
                eruptTplService.tplToResponse(operation.tpl(), response);
                break;
            }
        }
    }
}
