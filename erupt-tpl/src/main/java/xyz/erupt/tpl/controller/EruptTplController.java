package xyz.erupt.tpl.controller;


import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.tpl.engine.EngineConst;
import xyz.erupt.tpl.service.EruptTplService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.erupt.core.constant.EruptRestPath.ERUPT_API;

/**
 * Erupt 页面结构构建信息
 *
 * @author YuePeng
 * date 2018-09-28.
 */
@RestController
@RequestMapping(ERUPT_API + EruptTplController.TPL)
public class EruptTplController implements EruptRouter.VerifyHandler {

    static final String TPL = "/tpl";

    private static final String HTML_MIME_TYPE = "text/html;charset=utf-8";

    @Resource
    private EruptTplService eruptTplService;

    @GetMapping(value = "/**", produces = HTML_MIME_TYPE)
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyHandler = EruptTplController.class,
            verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void eruptTplPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = request.getRequestURI().split(ERUPT_API + EruptTplController.TPL + "/")[1];
        Method method = eruptTplService.getAction(path);
        if (null == method) {
            eruptTplService.tplRender(Tpl.Engine.Native, TPL + "/" + path, null, response.getWriter());
            return;
        }
        Object obj = EruptSpringUtil.getBean(method.getDeclaringClass());
        EruptTpl eruptTpl = obj.getClass().getAnnotation(EruptTpl.class);
        TplAction tplAction = method.getAnnotation(TplAction.class);
        path = TPL + "/" + path;
        if (StringUtils.isNotBlank(tplAction.path())) {
            path = tplAction.path();
        }
        eruptTplService.tplRender(eruptTpl.engine(), path, (Map) method.invoke(obj), response.getWriter());
    }

    @Override
    public String convertAuthStr(EruptRouter eruptRouter, HttpServletRequest request, String authStr) {
        return request.getRequestURI().split(ERUPT_API + EruptTplController.TPL + "/")[1];
    }

    @GetMapping(value = "/html-field/{erupt}/{field}", produces = HTML_MIME_TYPE)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void eruptFieldHtml(@PathVariable("erupt") String eruptName, @PathVariable("field") String field, HttpServletResponse response) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Tpl tpl = eruptModel.getEruptFieldMap().get(field).getEruptField().edit().tplType();
        eruptTplService.tplRender(tpl, null, response);
    }

    @GetMapping(value = "/operation_tpl/{erupt}/{code}", produces = HTML_MIME_TYPE)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void operationTpl(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
                                @RequestParam(value = "ids", required = false) String[] ids, HttpServletResponse response) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        RowOperation operation = Arrays.stream(eruptModel.getErupt().rowOperation()).filter(it ->
                it.code().equals(code)).findFirst().orElseThrow(EruptNoLegalPowerException::new);
        Erupts.powerLegal(ExprInvoke.getExpr(operation.show()));
        if (operation.tpl().engine() == Tpl.Engine.Native || operation.mode() == RowOperation.Mode.BUTTON) {
            eruptTplService.tplRender(operation.tpl(), null, response);
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(EngineConst.INJECT_ROWS, Stream.of(ids).map(id -> DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id))).collect(Collectors.toList()));
        eruptTplService.tplRender(operation.tpl(), map, response);
    }

    @GetMapping(value = "/view_tpl/{erupt}/{field}", produces = HTML_MIME_TYPE)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void viewTpl(@PathVariable("erupt") String eruptName, @PathVariable("field") String field, @RequestParam JsonObject row, HttpServletResponse response) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Tpl tpl = eruptModel.getEruptFieldMap().get(field).getEruptField().views()[0].tpl();
        Map<String, Object> map = new HashMap<>();
        map.put(EngineConst.INJECT_ROW, row);
        eruptTplService.tplRender(tpl, map, response);
    }

}
