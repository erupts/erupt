package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.event.EruptAddEvent;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2024/12/12 21:09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EruptModifyService {

    private final EruptService eruptService;

    private final HttpServletRequest request;

    private final ApplicationEventPublisher applicationEventPublisher;

    @SneakyThrows
    public Object eruptInsertDataProcess(EruptModel eruptModel, JsonObject data) {
        Map<String, Object> extraData = new HashMap<>();
        this.setLinkValue(eruptModel, extraData);
        this.setDrillValue(eruptModel, extraData);
        return EruptUtil.jsonToEruptEntity(eruptModel, data, extraData);
    }

    private void setLinkValue(EruptModel eruptModel, Map<String, Object> extraData) {
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field()) && dependTree.dependNode()) {
            String linkVal = request.getHeader("link");
            //必须是强依赖才会自动注入值
            if (dependTree.dependNode()) {
                if (StringUtils.isBlank(linkVal)) {
                    throw new EruptWebApiRuntimeException("Place select tree node");
                } else {
                    String rm = ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName();
                    JsonObject sub = new JsonObject();
                    sub.addProperty(EruptCoreService.getErupt(rm).getErupt().primaryKeyCol(), linkVal);
                    extraData.put(dependTree.field(), sub);
                }
            }
        }
    }

    private void setDrillValue(EruptModel eruptModel, Map<String, Object> extraData) {
        eruptService.drillProcess(eruptModel, (link, val) -> {
            String joinColumn = link.joinColumn();
            if (joinColumn.contains(EruptConst.DOT)) {
                String[] jc = joinColumn.split("\\.");
                JsonObject jo2 = new JsonObject();
                jo2.addProperty(jc[1], val.toString());
                extraData.put(jc[0], jo2);
            } else {
                extraData.put(joinColumn, val.toString());
            }
        });
    }

    public void modifyLog(EruptModel eruptModel, String action, String content) {
        log.info("ERUPT CURD | {} | {} | {} | {}", MetaContext.getUser().getName(), eruptModel.getEruptName(), action, content);
    }

    /**
     * @return pk
     */
    @SneakyThrows
    @Transactional
    public Object insertEruptData(EruptModel eruptModel, JsonObject data) {
        EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {
            throw new EruptApiErrorTip(eruptApiModel.getMessage(), EruptApiModel.PromptWay.MESSAGE);
        }
        Object obj = this.eruptInsertDataProcess(eruptModel, data);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeAdd(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, obj);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterAdd(obj)));
        applicationEventPublisher.publishEvent(new EruptAddEvent<>(eruptModel.getClazz(), obj));
        Object pk = ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(obj);
        this.modifyLog(eruptModel, "ADD", GsonFactory.getGson().toJson(obj));
        return pk;
    }

}
