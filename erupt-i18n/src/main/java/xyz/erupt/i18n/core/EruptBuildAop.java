package xyz.erupt.i18n.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.i18n.constant.I18nConstant;

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

    private static final String LANG_HEADER = "lang";
    private final String POINT_CUT = "execution(public * xyz.erupt.core.controller.EruptBuildController.getEruptBuild(..))";
    @Resource
    private HttpServletRequest request;

    @AfterReturning(pointcut = POINT_CUT, returning = "eruptBuildModel")
    public void doAfterReturning(EruptBuildModel eruptBuildModel) {
        if (StringUtils.isNotBlank(request.getHeader(LANG_HEADER))) {
            Optional.ofNullable(EruptCoreService.getErupt(MetaContext.getErupt().getName()).getClazz()).ifPresent(eruptClass -> {
                EruptI18n eruptI18n = eruptClass.getAnnotation(EruptI18n.class);
                if (null != eruptI18n && eruptI18n.enable()) {
                    Optional.ofNullable(I18nProcess.getLangMapping(request.getHeader(LANG_HEADER).toLowerCase())).ifPresent(it -> {
                        this.process(eruptBuildModel.getEruptModel(), it);
                        if (null != eruptBuildModel.getOperationErupts()) {
                            eruptBuildModel.getOperationErupts().values().forEach(eruptModel -> this.process(eruptModel, it));
                        }
                        if (null != eruptBuildModel.getTabErupts()) {
                            eruptBuildModel.getTabErupts().values().forEach(eruptModel -> this.process(eruptModel.getEruptModel(), it));
                        }
                    });
                }
            });
        }
    }

    private void process(EruptModel eruptModel, Properties langMapping) {
        eruptModel.setEruptJson(eruptModel.getEruptJson().deepCopy());
        JsonObject eruptJson = eruptModel.getEruptJson();
        if (eruptJson.has(I18nConstant.ROW_OPERATION)) {
            for (JsonElement rowOperation : eruptJson.getAsJsonArray(I18nConstant.ROW_OPERATION)) {
                this.convert(langMapping, rowOperation.getAsJsonObject(), I18nConstant.TITLE);
                this.convert(langMapping, rowOperation.getAsJsonObject(), I18nConstant.TIP);
            }
        }
        if (eruptJson.has(I18nConstant.ROW_OPERATION)) {
            for (JsonElement drill : eruptJson.getAsJsonArray(I18nConstant.DRILLS)) {
                this.convert(langMapping, drill.getAsJsonObject(), I18nConstant.TITLE);
            }
        }
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            fieldModel.setEruptFieldJson(fieldModel.getEruptFieldJson().deepCopy());
            JsonObject eruptFieldJson = fieldModel.getEruptFieldJson();
            if (eruptFieldJson.has(I18nConstant.EDIT)) {
                JsonObject edit = eruptFieldJson.getAsJsonObject(I18nConstant.EDIT);
                this.convert(langMapping, edit, I18nConstant.TITLE);
                this.convert(langMapping, edit, I18nConstant.DESC);
            }
            if (eruptFieldJson.has(I18nConstant.VIEWS)) {
                for (JsonElement view : eruptFieldJson.getAsJsonArray(I18nConstant.VIEWS)) {
                    this.convert(langMapping, view.getAsJsonObject(), I18nConstant.TITLE);
                    this.convert(langMapping, view.getAsJsonObject(), I18nConstant.DESC);
                }
            }
        }
    }

    public void convert(Properties langMapping, JsonObject element, String replaceKey) {
        String value = element.get(replaceKey).getAsString();
        if (StringUtils.isNotBlank(value)) {
            if (langMapping.containsKey(value)) {
                element.addProperty(replaceKey, langMapping.get(value).toString());
            }
        }
    }

}
