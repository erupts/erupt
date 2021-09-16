package xyz.erupt.i18n.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.i18n.constant.I18nConstant;
import xyz.erupt.i18n.service.I18nService;
import xyz.erupt.upms.service.EruptContextService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Properties;

/**
 * @author YuePeng
 * date 2021/9/12 23:55
 */
@Aspect
@Component
public class EruptBuildAop {

    private final String POINT_CUT = "execution(public * xyz.erupt.core.controller.EruptBuildController.getEruptBuild(..))";

    private static final String LANG_HEADER = "lang";

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptContextService eruptContextService;

    @AfterReturning(pointcut = POINT_CUT, returning = "eruptBuildModel")
    public void doAfterReturning(EruptBuildModel eruptBuildModel) {
        if (!StringUtils.isNotBlank(request.getHeader(LANG_HEADER))) {
            Optional.ofNullable(eruptContextService.getContextEruptClass()).ifPresent(eruptClass -> {
                EruptI18n eruptI18n = eruptClass.getAnnotation(EruptI18n.class);
                if (null != eruptI18n && eruptI18n.enable()) {
                    Optional.ofNullable(I18nService.getLangMapping("en")).ifPresent(it -> {
                        this.i18nProcess(eruptBuildModel.getEruptModel(), it);
                        if (null != eruptBuildModel.getOperationErupts()) {
                            eruptBuildModel.getOperationErupts().values().forEach(eruptModel -> this.i18nProcess(eruptModel, it));
                        }
                        if (null != eruptBuildModel.getTabErupts()) {
                            eruptBuildModel.getTabErupts().values().forEach(eruptModel -> this.i18nProcess(eruptModel.getEruptModel(), it));
                        }
                    });
                }
            });
        }
    }

    private void i18nProcess(EruptModel eruptModel, Properties langMapping) {
        JsonObject eruptJson = eruptModel.getEruptJson();

        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            JsonObject eruptFieldJson = fieldModel.getEruptFieldJson();
            if (eruptFieldJson.has(I18nConstant.EDIT)) {
                JsonObject edit = eruptFieldJson.getAsJsonObject(I18nConstant.EDIT);
                String title = edit.get(I18nConstant.TITLE).getAsString();
                if (langMapping.containsKey(title)) {
                    edit.addProperty(I18nConstant.TITLE, langMapping.get(title).toString());
                }
                if (EditType.BOOLEAN.name().equals(edit.get(I18nConstant.TYPE).getAsString())) {
                    JsonObject boolType = edit.getAsJsonObject(I18nConstant.BOOL_TYPE);
                    String trueText = boolType.get(I18nConstant.TRUE_TEXT).getAsString();
                    String falseText = boolType.get(I18nConstant.FALSE_TEXT).getAsString();
                    if (langMapping.containsKey(trueText)) {
                        boolType.addProperty(I18nConstant.TRUE_TEXT, langMapping.get(trueText).toString());
                    }
                    if (langMapping.containsKey(falseText)) {
                        boolType.addProperty(I18nConstant.FALSE_TEXT, langMapping.get(falseText).toString());
                    }
                }
            }
            if (eruptFieldJson.has(I18nConstant.VIEWS)) {
                for (JsonElement element : eruptFieldJson.getAsJsonArray(I18nConstant.VIEWS)) {
                    String title = element.getAsJsonObject().get(I18nConstant.TITLE).getAsString();
                    if (langMapping.containsKey(title)) {
                        element.getAsJsonObject().addProperty(I18nConstant.TITLE, langMapping.get(title).toString());
                    }
                }
            }
        }
    }


    private void eruptJsonI18n(JsonObject eruptJson, Properties langMapping) {

    }

    public String convert(String key) {
        return key;
    }

}
