package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.EruptFieldModel;
import com.google.gson.JsonElement;
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
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
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
        // erupt-cloud: forward to the owning node, which runs its own validation/DataProxy pipeline
        if (eruptModel.isRemote()) {
            return EruptRemoteRouterManager.get().insert(eruptModel.getEruptName(), data);
        }
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

    /**
     * Update a single field of one row — the write behind in-table cell editing.
     * <p>
     * The whole edit pipeline is reused (permission check, whole-row validation, DataProxy
     * before/after hooks, operate log, edit event); only the target field differs from the
     * stored row, so readonly, PASSWORD and collection semantics of {@link EruptUtil#dataTarget}
     * apply unchanged to every other field.
     */
    @SneakyThrows
    @Transactional
    public void updateEruptCell(EruptModel eruptModel, String id, String fieldName, JsonElement value) {
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        if (null == fieldModel || StringUtils.isBlank(fieldModel.getEruptField().edit().title())) {
            throw new EruptApiErrorTip(I18nTranslate.$translate("erupt.cell.not_editable") + ": " + fieldName, R.PromptWay.MESSAGE);
        }
        // the field may opt out even when its model allows cell editing
        if (!fieldModel.getEruptField().edit().cellEdit()) {
            throw new EruptApiErrorTip(I18nTranslate.$translate("erupt.cell.not_editable") + ": " + fieldName, R.PromptWay.MESSAGE);
        }
        // a cell writes exactly where the row form offers an enabled control, so a field the form
        // renders read-only is refused whatever allowChange says: that flag exists so a row
        // operation or handler can still set the value, not so a hand-built cell patch can
        if (fieldModel.getEruptField().edit().readonly().edit()) {
            throw new EruptApiErrorTip(I18nTranslate.$translate("erupt.cell.not_editable") + ": " + fieldName, R.PromptWay.MESSAGE);
        }
        if (eruptModel.isRemote()) {
            // the owning node runs its own validation / DataProxy pipeline
            JsonObject remote = new JsonObject();
            remote.addProperty(eruptModel.getErupt().primaryKeyCol(), id);
            remote.add(fieldName, value);
            EruptRemoteRouterManager.get().update(eruptModel.getEruptName(), remote);
            return;
        }
        eruptService.verifyIdPermissions(eruptModel, id);
        Field pkField = ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol());
        Object old = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .findDataById(eruptModel, TypeUtil.typeStrConvertObject(id, pkField.getType()));
        if (null == old) {
            throw new EruptApiErrorTip(I18nTranslate.$translate("erupt.cell.row_not_exist") + ": " + id, R.PromptWay.MESSAGE);
        }
        // the stored row patched with the new value is validated as a whole, so a single cell runs
        // exactly the rules the edit form runs: every field's own rules, a @Dynamic rule that reads
        // another field, and DataProxy#validate against a complete entity
        JsonObject merged = GsonFactory.getGson().toJsonTree(old).getAsJsonObject();
        merged.add(fieldName, value);
        R<Void> validation = EruptUtil.validateEruptValue(eruptModel, merged);
        if (!validation.isSuccess()) {
            throw new EruptApiErrorTip(validation.getMessage(), R.PromptWay.MESSAGE);
        }
        Object realOld = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        // reflective copy: runtime-generated carrier classes (erupt-designer) have no accessors
        EruptUtil.copyEruptFields(eruptModel, old, realOld);
        String oldData;
        try {
            oldData = EruptUtil.toMaskedJson(eruptModel, realOld);
        } catch (Exception e) {
            oldData = GsonFactory.getGson().toJson(realOld);
        }
        OldEntityTL.set(oldData);
        // carrier holds the patched field only; every other field of the stored row is left as is
        Object data = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        Field field = fieldModel.getField();
        field.setAccessible(true);
        field.set(data, value.isJsonNull() ? null
                : GsonFactory.getGson().fromJson(value, field.getGenericType()));
        EruptUtil.dataTargetField(fieldModel, data, old, SceneEnum.EDIT);
        Object obj = old;
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
        JsonObject maskedCell = new JsonObject();
        maskedCell.add(fieldName, value);
        EruptUtil.maskPasswordFields(eruptModel, maskedCell);
        this.modifyLog(eruptModel, "UPDATE", oldData + " -> " + maskedCell);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));
        applicationEventPublisher.publishEvent(new EruptEditEvent<>(eruptModel.getClazz(), obj, realOld));
    }

    @SneakyThrows
    @Transactional
    public void updateEruptData(EruptModel eruptModel, JsonObject data) {
        if (eruptModel.isRemote()) {
            EruptRemoteRouterManager.get().update(eruptModel.getEruptName(), data);
            return;
        }
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
        String oldData;
        try {
            oldData = EruptUtil.toMaskedJson(eruptModel, realOld);
        } catch (Exception e) {
            oldData = GsonFactory.getGson().toJson(realOld);
        }
        OldEntityTL.set(oldData);
        Object obj = EruptUtil.dataTarget(eruptModel, o, old, SceneEnum.EDIT);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
        // Mask PASSWORD fields on both sides so plaintext credentials never reach the log
        JsonObject maskedData = data.deepCopy();
        EruptUtil.maskPasswordFields(eruptModel, maskedData);
        this.modifyLog(eruptModel, "UPDATE", oldData + " -> " + maskedData);
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
        if (eruptModel.isRemote()) {
            EruptRemoteRouterManager.get().delete(eruptModel.getEruptName(), ids);
            return;
        }
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
