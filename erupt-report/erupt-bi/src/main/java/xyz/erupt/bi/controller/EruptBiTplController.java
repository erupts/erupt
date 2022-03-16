package xyz.erupt.bi.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiTpl;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.tpl.engine.FreemarkerEngine;
import xyz.erupt.tpl.service.EruptTplService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2022/1/1 23:46
 */
@RestController
@RequestMapping(BiConst.BATH_PATH)
public class EruptBiTplController {

    @Resource
    private BiService biService;

    @Resource
    private EruptTplService eruptTplService;

    @PersistenceContext
    private EntityManager entityManager;

    private final Gson gson = GsonFactory.getGson();

    private final Configuration freemarker = new FreemarkerEngine().init();

    private static final String HTML = "" +
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title></title>\n" +
            "</head>\n" +
            "<body>\n" +
            BiConst.SIMPLE_PLACEHOLDER +
            "</body>\n" +
            "</html>";

    @PostConstruct
    public void post() {
        freemarker.setTemplateLoader(new StringTemplateLoader());
    }

    @GetMapping(value = "/{code}/custom-chart/{id}", produces = {"text/html;charset=UTF-8"})
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void customerChart(@PathVariable String code,
                              @PathVariable("id") Long chartId,
                              @RequestParam("condition") String conditionStr,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException, TemplateException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> condition = gson.fromJson(URLDecoder.decode(conditionStr,
                StandardCharsets.UTF_8.name()), new TypeToken<Map<String, Object>>() {
        }.getType());
        BiChart biChart = entityManager.find(BiChart.class, chartId);
        biService.verifyBiMenuPermissions(biChart.getBi(), code);
        if (null == biChart.getBiTpl()) throw new EruptWebApiRuntimeException("Tpl not config");
        Map<String, Object> map = new HashMap<>();
        String data = "data";
        map.put(data, biService.startQuery(biChart.getName(),biChart.getSqlStatement(), biChart.getCacheTime(), biChart.getClassHandler(), biChart.getDataSource(), condition));
        map.put("dataJson", gson.toJson(map.get(data)));
        if (BiTpl.TYPE_ONLINE.equals(biChart.getBiTpl().getType())) {
            map.put("request", request);
            StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) freemarker.getTemplateLoader();
            stringTemplateLoader.putTemplate(biChart.getCode(), HTML.replace(BiConst.SIMPLE_PLACEHOLDER, biChart.getBiTpl().getTpl()));
            freemarker.getTemplate(biChart.getCode(), "utf-8").process(map, response.getWriter());
        } else if (BiTpl.TYPE_PATH.equals(biChart.getBiTpl().getType())) {
            eruptTplService.tplRender(Tpl.Engine.FreeMarker, biChart.getBiTpl().getPath(), map, response.getWriter());
        }

    }

}
