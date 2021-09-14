package xyz.erupt.i18n.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.i18n.constant.I18nConstant;
import xyz.erupt.upms.service.EruptContextService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author YuePeng
 * date 2021/9/12 23:55
 */
@Aspect
@Component
public class EruptBuildAop {

    private final String POINT_CUT = "execution(public * xyz.erupt.core.controller.EruptBuildController.getEruptBuild(..))";

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptContextService eruptContextService;

    @AfterReturning(pointcut = POINT_CUT, returning = "eruptBuildModel")
    public void doAfterReturning(EruptBuildModel eruptBuildModel) {
        Class<?> eruptClass = eruptContextService.getContextEruptClass();
        if (null != eruptClass) {
            EruptI18n eruptI18n = eruptClass.getAnnotation(EruptI18n.class);
            if (null != eruptI18n && eruptI18n.enable()) {
                this.eruptJsonI18n(eruptBuildModel.getEruptModel().getEruptJson());
                for (EruptFieldModel fieldModel : eruptBuildModel.getEruptModel().getEruptFieldModels()) {
                    this.eruptFieldJsonI18n(fieldModel.getEruptFieldJson());
                }
            }
        }
    }

    private void eruptFieldJsonI18n(JsonObject eruptFieldJson) {
        if (eruptFieldJson.has(I18nConstant.EDIT)) {
            eruptFieldJson.getAsJsonObject(I18nConstant.EDIT).addProperty(I18nConstant.TITLE, 1);
        }
        if (eruptFieldJson.has(I18nConstant.VIEWS)) {
            for (JsonElement element : eruptFieldJson.getAsJsonArray(I18nConstant.VIEWS)) {
//                element.getAsJsonObject().addProperty(I18nConstant.TITLE, 1);
            }
        }
    }

    private void eruptJsonI18n(JsonObject eruptJson) {

    }

    public String convert(String key) {
        return key;
    }

}
