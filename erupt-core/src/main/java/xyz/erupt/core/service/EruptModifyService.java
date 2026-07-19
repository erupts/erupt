package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.constant.SceneEnum;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.OldEntityTL;
import xyz.erupt.core.event.EruptAddEvent;
import xyz.erupt.core.event.EruptDeleteEvent;
import xyz.erupt.core.event.EruptEditEvent;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (EruptSpringUtil.isMvcContext()) {
            this.setLinkValue(eruptModel, extraData);
            this.setDrillValue(eruptModel, extraData);
        }
        return EruptUtil.jsonToEruptEntity(eruptModel, data, extraData);
    }

    private void setLinkValue(EruptModel eruptModel, Map<String, Object> extraData) {
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field()) && dependTree.dependNode()) {
            String linkVal = request.getHeader("link");
            // Value will only be auto-injected for strong dependencies
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
        R<Void> validation = EruptUtil.validateEruptValue(eruptModel, data);
        if (!validation.isSuccess()) {
            throw new EruptApiErrorTip(validation.getMessage(), R.PromptWay.MESSAGE);
        }
        Object obj = this.eruptInsertDataProcess(eruptModel, data);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeAdd(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, obj);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterAdd(obj)));
        applicationEventPublisher.publishEvent(new EruptAddEvent<>(eruptModel.getClazz(), obj));
        Object pk = ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(obj);
        this.modifyLog(eruptModel, "ADD", EruptUtil.toMaskedJson(eruptModel, obj));
        return pk;
    }

    @SneakyThrows
    @Transactional
    public void updateEruptData(EruptModel eruptModel, JsonObject data) {
        R<Void> validation = EruptUtil.validateEruptValue(eruptModel, data);
        if (!validation.isSuccess()) {
            throw new EruptApiErrorTip(validation.getMessage(), R.PromptWay.MESSAGE);
        }
        eruptService.verifyIdPermissions(eruptModel, data.get(eruptModel.getErupt().primaryKeyCol()).getAsString());
        Object o = GsonFactory.getGson().fromJson(data.toString(), eruptModel.getClazz());
        EruptUtil.clearObjectDefaultValueByJson(o, data);
        Object old = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(o));
        Object realOld = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(old, realOld);
        String maskedOld = EruptUtil.toMaskedJson(eruptModel, realOld);
        OldEntityTL.set(maskedOld);
        Object obj = EruptUtil.dataTarget(eruptModel, o, old, SceneEnum.EDIT);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
        // Mask PASSWORD fields on both sides so plaintext credentials never reach the log
        JsonObject maskedData = data.deepCopy();
        EruptUtil.maskPasswordFields(eruptModel, maskedData);
        this.modifyLog(eruptModel, "UPDATE", maskedOld + " -> " + maskedData);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));
        applicationEventPublisher.publishEvent(new EruptEditEvent<>(eruptModel.getClazz(), obj, realOld));
    }

    @SneakyThrows
    @Transactional
    public void dragSortEruptData(EruptModel eruptModel, Map<String, Object> sortData) {
        String sortField = eruptModel.getErupt().dragSort().field();
        if (StringUtils.isBlank(sortField)) {
            throw new EruptWebApiRuntimeException("dragSort is not enabled");
        }
        IEruptDataService dataService = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());
        Field field = ReflectUtil.findClassField(eruptModel.getClazz(), sortField);
        for (Map.Entry<String, Object> entry : sortData.entrySet()) {
            eruptService.verifyIdPermissions(eruptModel, entry.getKey());
            Object obj = dataService.findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, entry.getKey()));
            field.set(obj, TypeUtil.typeStrConvertObject(entry.getValue(), field.getType()));
            dataService.editData(eruptModel, obj);
        }
        this.modifyLog(eruptModel, "SORT", sortData.toString());
    }

    @SneakyThrows
    @Transactional
    public void deleteEruptData(EruptModel eruptModel, List<Object> ids, boolean verifyIdPermissions) {
        List<Object> deletedObjs = new ArrayList<>();
        for (Object id : ids) {
            if (verifyIdPermissions) {
                eruptService.verifyIdPermissions(eruptModel, id.toString());
            }
            IEruptDataService dataService = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());
            // Retrieve object data for use in DataProxy callbacks
            Object obj = dataService.findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id.toString()));
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeDelete(obj)));
            dataService.deleteData(eruptModel, obj);
            this.modifyLog(eruptModel, "DELETE", EruptUtil.toMaskedJson(eruptModel, obj));
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterDelete(obj)));
            applicationEventPublisher.publishEvent(new EruptDeleteEvent<>(eruptModel.getClazz(), obj));
            deletedObjs.add(obj);
        }
        OldEntityTL.set(EruptUtil.toMaskedJson(eruptModel, deletedObjs.size() == 1 ? deletedObjs.get(0) : deletedObjs));
    }

}
