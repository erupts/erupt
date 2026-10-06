package xyz.erupt.revision.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.event.EruptAddEvent;
import xyz.erupt.core.event.EruptDeleteEvent;
import xyz.erupt.core.event.EruptEditEvent;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.model.RevisionOperation;
import xyz.erupt.revision.pojo.FieldChange;
import xyz.erupt.revision.pojo.RevisionVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Captures a revision for every record change that goes through the erupt pipeline and serves
 * the history back to the record panel. The listeners run inside the caller's transaction, so
 * a change and its revision commit or roll back together.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class EruptRevisionService {

    private static final Gson GSON = GsonFactory.getGson();

    // key of the display label kept next to a reference's primary key in a stored change
    private static final String LABEL = "label";

    private static final Set<EditType> REFERENCE_TYPES = EnumSet.of(EditType.REFERENCE_TABLE, EditType.REFERENCE_TREE,
            EditType.CHECKBOX, EditType.TRANSFER, EditType.TAB_TREE, EditType.TAB_TABLE_REFER);

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptModifyService eruptModifyService;

    // ---------- capture ----------

    @EventListener
    @Transactional
    public void onAdd(EruptAddEvent<Object> event) {
        this.capture(event.getEruptClass(), RevisionOperation.ADD, null, event.getSource());
    }

    @EventListener
    @Transactional
    public void onEdit(EruptEditEvent<Object> event) {
        this.capture(event.getEruptClass(), RevisionOperation.UPDATE, event.getBefore(), event.getSource());
    }

    @EventListener
    @Transactional
    public void onDelete(EruptDeleteEvent<Object> event) {
        this.capture(event.getEruptClass(), RevisionOperation.DELETE, event.getSource(), null);
    }

    private void capture(Class<?> clazz, RevisionOperation operation, Object before, Object after) {
        EruptModel eruptModel = this.resolve(clazz);
        if (null == eruptModel || !eruptModel.getErupt().power().revision()) return;
        try {
            JsonObject beforeJson = this.masked(eruptModel, before);
            JsonObject afterJson = this.masked(eruptModel, after);
            List<FieldChange> changes = this.diff(eruptModel, beforeJson, afterJson);
            if (changes.isEmpty()) return;
            String recordId = this.recordId(eruptModel, null == after ? beforeJson : afterJson);
            if (null == recordId) return;
            EruptRecordRevision revision = new EruptRecordRevision();
            revision.setErupt(eruptModel.getEruptName());
            revision.setRecordId(recordId);
            revision.setOperation(operation);
            revision.setVersion(this.nextVersion(eruptModel.getEruptName(), recordId));
            revision.setOperator(this.operatorName());
            revision.setChanges(GSON.toJson(changes));
            eruptDao.persist(revision);
        } catch (Exception e) {
            // the history must never take the change itself down
            log.warn("erupt-revision: failed to record {} of {}: {}", operation, eruptModel.getEruptName(), e.getMessage());
        }
    }

    // The revision table itself and anything that is not a registered erupt are left alone.
    private EruptModel resolve(Class<?> clazz) {
        if (null == clazz || EruptRecordRevision.class == clazz) return null;
        EruptModel eruptModel = EruptCoreService.getErupt(clazz.getSimpleName());
        if (null != eruptModel && eruptModel.getClazz() == clazz) return eruptModel;
        return EruptCoreService.getErupts().stream().filter(it -> it.getClazz() == clazz).findFirst().orElse(null);
    }

    private JsonObject masked(EruptModel eruptModel, Object obj) {
        if (null == obj) return null;
        JsonElement element = GSON.fromJson(EruptUtil.toMaskedJson(eruptModel, obj), JsonElement.class);
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private String recordId(EruptModel eruptModel, JsonObject json) {
        if (null == json) return null;
        JsonElement pk = json.get(eruptModel.getErupt().primaryKeyCol());
        return null == pk || pk.isJsonNull() ? null : pk.getAsString();
    }

    // Field by field over the erupt fields only; a value absent on one side counts as null.
    private List<FieldChange> diff(EruptModel eruptModel, JsonObject before, JsonObject after) {
        List<FieldChange> changes = new ArrayList<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            String name = fieldModel.getFieldName();
            JsonElement b = this.compact(fieldModel, this.valueOf(before, name));
            JsonElement a = this.compact(fieldModel, this.valueOf(after, name));
            // a reference is the same reference when its key is: a side that only carries the key
            // (a cell edit, a rollback) must not read as a change of the label; an empty collection,
            // object or string is the same nothing as null, since the form sends null for them
            if (Objects.equals(this.comparable(b), this.comparable(a))) continue;
            String title = fieldModel.getEruptField().edit().title();
            if (title.isEmpty() && fieldModel.getEruptField().views().length > 0) {
                title = fieldModel.getEruptField().views()[0].title();
            }
            changes.add(new FieldChange(name, title.isEmpty() ? name : title, b, a));
        }
        return changes;
    }

    // A picked reference is stored as its key plus display label rather than the whole referenced
    // row: that is all the panel shows and all a rollback needs to pick it again. Owned sub-rows
    // (TAB_TABLE_ADD, MULTI_FORM, COMBINE) keep their full JSON, since a rollback must rebuild them.
    private JsonElement compact(EruptFieldModel fieldModel, JsonElement value) {
        if (null == value || !REFERENCE_TYPES.contains(fieldModel.getEruptField().edit().type())) return value;
        EruptModel referenced = EruptCoreService.getErupt(fieldModel.getFieldReturnName());
        if (null == referenced) return value;
        if (value.isJsonObject()) return this.pick(fieldModel, referenced, value.getAsJsonObject());
        if (!value.isJsonArray()) return value;
        JsonArray picked = new JsonArray();
        for (JsonElement item : value.getAsJsonArray()) {
            picked.add(item.isJsonObject() ? this.pick(fieldModel, referenced, item.getAsJsonObject()) : item);
        }
        return picked;
    }

    private JsonElement comparable(JsonElement value) {
        if (null == value || value.isJsonNull()) return null;
        if (value.isJsonArray() && value.getAsJsonArray().isEmpty()) return null;
        if (value.isJsonObject() && value.getAsJsonObject().isEmpty()) return null;
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() && value.getAsString().isEmpty()) return null;
        return this.withoutLabel(value);
    }

    private JsonElement withoutLabel(JsonElement value) {
        if (null == value) return null;
        if (value.isJsonObject()) {
            JsonObject copy = value.getAsJsonObject().deepCopy();
            copy.remove(LABEL);
            return copy;
        }
        if (value.isJsonArray()) {
            JsonArray copy = new JsonArray();
            for (JsonElement item : value.getAsJsonArray()) copy.add(this.withoutLabel(item));
            return copy;
        }
        return value;
    }

    private JsonObject pick(EruptFieldModel fieldModel, EruptModel referenced, JsonObject row) {
        String pkCol = referenced.getErupt().primaryKeyCol();
        JsonObject picked = new JsonObject();
        picked.add(pkCol, row.get(pkCol));
        JsonElement label = row.get(this.labelColumn(fieldModel, referenced, row));
        if (null != label && label.isJsonPrimitive()) picked.add(LABEL, label);
        return picked;
    }

    // the column the picker labels rows with (the conventional "name" for the other pickers), when
    // the row carries it; else the first shown string column
    private String labelColumn(EruptFieldModel fieldModel, EruptModel referenced, JsonObject row) {
        Edit edit = fieldModel.getEruptField().edit();
        String label = switch (edit.type()) {
            case REFERENCE_TABLE -> edit.referenceTableType().label();
            case REFERENCE_TREE -> edit.referenceTreeType().label();
            default -> AnnotationConst.LABEL;
        };
        if (StringUtils.isNotBlank(label) && row.has(label)) return label;
        return referenced.getEruptFieldModels().stream()
                .filter(it -> String.class.getSimpleName().equals(it.getFieldReturnName()))
                .filter(it -> Arrays.stream(it.getEruptField().views()).anyMatch(View::show))
                .map(EruptFieldModel::getFieldName).filter(row::has).findFirst().orElse(referenced.getErupt().primaryKeyCol());
    }

    private JsonElement valueOf(JsonObject json, String name) {
        if (null == json) return null;
        JsonElement value = json.get(name);
        return null == value || value.isJsonNull() ? null : value;
    }

    private Integer nextVersion(String erupt, String recordId) {
        Integer max = eruptDao.getEntityManager().createQuery(
                        "select max(r.version) from EruptRecordRevision r where r.erupt = :erupt and r.recordId = :id", Integer.class)
                .setParameter("erupt", erupt).setParameter("id", recordId).getSingleResult();
        return null == max ? 1 : max + 1;
    }

    private String operatorName() {
        try {
            MetaUser user = MetaContext.getUser();
            return null == user ? null : user.getName();
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- read ----------

    public List<RevisionVo> list(String erupt, String recordId) {
        return eruptDao.lambdaQuery(EruptRecordRevision.class)
                .eq(EruptRecordRevision::getErupt, erupt)
                .eq(EruptRecordRevision::getRecordId, recordId)
                .orderByDesc(EruptRecordRevision::getVersion)
                .list().stream().map(RevisionVo::of).toList();
    }

    // ---------- rollback ----------

    /**
     * Puts the {@code before} value of every field the revision changed back onto the current
     * record, through the regular update pipeline: validation, DataProxy hooks and a fresh
     * revision row all happen as for a form save. Fields the revision did not touch keep their
     * current value, so a rollback of an old revision undoes that one change only.
     */
    @Transactional
    public void rollback(String erupt, String recordId, Long revisionId) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        if (null == eruptModel) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("revision.model_not_found"));
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        EruptRecordRevision revision = eruptDao.find(EruptRecordRevision.class, revisionId);
        if (null == revision || !erupt.equals(revision.getErupt()) || !recordId.equals(revision.getRecordId())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("revision.not_found"));
        }
        if (revision.getOperation() != RevisionOperation.UPDATE) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("revision.rollback_update_only"));
        }
        Object current = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, recordId));
        if (null == current) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("revision.record_not_found"));
        JsonObject data = GSON.toJsonTree(current).getAsJsonObject();
        for (FieldChange change : RevisionVo.parse(revision.getChanges())) {
            EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(change.getField());
            // a field removed from the model since, or a masked password, cannot be restored
            if (null == fieldModel || fieldModel.getEruptField().edit().type() == EditType.PASSWORD) continue;
            if (null != change.getBefore() && change.getBefore().isJsonPrimitive()
                    && EruptConst.PASSWORD_PLACEHOLDER.equals(change.getBefore().getAsString())) continue;
            data.add(change.getField(), null == change.getBefore() ? JsonNull.INSTANCE : change.getBefore());
        }
        eruptModifyService.updateEruptData(eruptModel, data);
    }

    /**
     * Reflection helper kept for callers that hold the entity rather than its JSON.
     */
    public Object primaryKey(EruptModel eruptModel, Object obj) throws IllegalAccessException {
        return ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(obj);
    }
}
