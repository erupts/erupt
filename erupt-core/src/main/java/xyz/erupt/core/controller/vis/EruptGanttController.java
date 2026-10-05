package xyz.erupt.core.controller.vis;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.OldEntityTL;
import xyz.erupt.core.event.EruptEditEvent;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Writes made from the gantt view: bar drags change the dates, link drags change the
 * predecessors declared by {@code GanttView#dependencyField}. Both run the DataProxy update
 * hooks and publish {@link EruptEditEvent} with the pre-change copy, so the operation log and
 * other modules see them like any edit.
 *
 * @author YuePeng
 * date 2025/11/15 00:25
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY + "/gantt")
@RequiredArgsConstructor
public class EruptGanttController {

    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/{erupt}/update_date")
    @EruptRouter(skipAuthIndex = 4, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    @SneakyThrows
    public R<Void> updateDate(@PathVariable("erupt") String erupt, @RequestBody GanttDateCommand command) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        Vis vis = this.findVis(eruptModel, command.getVisCode());
        Object obj = this.findById(eruptModel, command.getPk());
        Object before = this.snapshot(eruptModel, obj, null);
        Field startField = ReflectUtil.findClassField(obj.getClass(), vis.ganttView().startDateField());
        Field endField = ReflectUtil.findClassField(obj.getClass(), vis.ganttView().endDateField());
        startField.set(obj, DateUtil.getDate(startField.getType(), command.getStartDate()));
        endField.set(obj, DateUtil.getDate(endField.getType(), command.getEndDate()));
        this.update(eruptModel, obj, before);
        return R.ok();
    }

    // predecessor ids of the given rows, keyed by row id; the chart draws them as finish-to-start links
    @PostMapping("/{erupt}/links")
    @EruptRouter(skipAuthIndex = 4, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    @SneakyThrows
    public R<Map<String, List<String>>> links(@PathVariable("erupt") String erupt, @RequestBody GanttLinksCommand command) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Vis vis = this.findVis(eruptModel, command.getVisCode());
        Map<String, List<String>> links = new LinkedHashMap<>();
        if (StringUtils.isBlank(vis.ganttView().dependencyField()) || null == command.getPks()) return R.ok(links);
        Field depField = ReflectUtil.findClassField(eruptModel.getClazz(), vis.ganttView().dependencyField());
        for (Object pk : command.getPks()) {
            Object obj = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, pk);
            if (null == obj) continue;
            List<String> ids = new ArrayList<>();
            for (Object predecessor : this.predecessors(depField, obj)) ids.add(String.valueOf(this.pk(eruptModel, predecessor)));
            links.put(String.valueOf(pk), ids);
        }
        return R.ok(links);
    }

    // add or remove one predecessor of a row; a self link or a cycle is rejected
    @PostMapping("/{erupt}/link")
    @EruptRouter(skipAuthIndex = 4, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    @SneakyThrows
    public R<Void> link(@PathVariable("erupt") String erupt, @RequestBody GanttLinkCommand command) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        Vis vis = this.findVis(eruptModel, command.getVisCode());
        if (StringUtils.isBlank(vis.ganttView().dependencyField())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.gantt.dependency_unconfigured"));
        }
        if (String.valueOf(command.getPk()).equals(String.valueOf(command.getPredecessorPk()))) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.gantt.dependency_self"));
        }
        Field depField = ReflectUtil.findClassField(eruptModel.getClazz(), vis.ganttView().dependencyField());
        Object obj = this.findById(eruptModel, command.getPk());
        // loaded as the field's element type, which may be a lighter projection of the same table
        Object predecessor = this.findById(this.dependencyModel(eruptModel, depField), command.getPredecessorPk());
        Object before = this.snapshot(eruptModel, obj, depField);
        String pk = String.valueOf(this.pk(eruptModel, obj));
        if (command.isRemove()) {
            if (Collection.class.isAssignableFrom(depField.getType())) {
                Collection<Object> deps = (Collection<Object>) depField.get(obj);
                if (null != deps) deps.removeIf(it -> String.valueOf(this.pk(eruptModel, it)).equals(String.valueOf(command.getPredecessorPk())));
            } else {
                depField.set(obj, null);
            }
        } else {
            // the new predecessor must not already depend on this row, directly or through others
            if (this.reaches(eruptModel, depField, predecessor, pk, new HashSet<>())) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.gantt.dependency_cycle"));
            }
            if (Collection.class.isAssignableFrom(depField.getType())) {
                Collection<Object> deps = (Collection<Object>) depField.get(obj);
                if (null == deps) {
                    deps = Set.class.isAssignableFrom(depField.getType()) ? new LinkedHashSet<>() : new ArrayList<>();
                    depField.set(obj, deps);
                }
                boolean present = deps.stream().anyMatch(it -> String.valueOf(this.pk(eruptModel, it)).equals(String.valueOf(command.getPredecessorPk())));
                if (!present) deps.add(predecessor);
            } else {
                depField.set(obj, predecessor);
            }
        }
        this.update(eruptModel, obj, before);
        return R.ok();
    }

    // the erupt model the dependency field points at: a collection's element type or the reference
    // type itself, falling back to the model when the type is not an erupt of its own
    private EruptModel dependencyModel(EruptModel eruptModel, Field depField) {
        String typeName = Collection.class.isAssignableFrom(depField.getType())
                ? ReflectUtil.getFieldGenericName(depField).stream().findFirst().orElse(null)
                : depField.getType().getSimpleName();
        return Optional.ofNullable(typeName).map(EruptCoreService::getErupt).orElse(eruptModel);
    }

    private Vis findVis(EruptModel eruptModel, String visCode) {
        return Arrays.stream(eruptModel.getErupt().vis()).filter(it -> it.code().equals(visCode)).findFirst()
                .orElseThrow(() -> new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.gantt.view_not_found")));
    }

    private Object findById(EruptModel eruptModel, Object pk) {
        Object obj = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, pk);
        if (null == obj) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.gantt.record_not_found"));
        return obj;
    }

    // read off the object's own class: a predecessor may be a lighter projection of the same table
    @SneakyThrows
    private Object pk(EruptModel eruptModel, Object obj) {
        return ReflectUtil.findClassField(obj.getClass(), eruptModel.getErupt().primaryKeyCol()).get(obj);
    }

    // predecessors of a row for either field shape: a single reference or a collection of them
    @SneakyThrows
    private Collection<Object> predecessors(Field depField, Object obj) {
        Object value = depField.get(obj);
        if (null == value) return Collections.emptyList();
        return value instanceof Collection ? (Collection<Object>) value : Collections.singletonList(value);
    }

    // true when walking the predecessor chain from `from` meets the row `targetPk`; a predecessor
    // that is not an instance of the model (a projection entity) is reloaded as the model by its id
    private boolean reaches(EruptModel eruptModel, Field depField, Object from, String targetPk, Set<String> seen) {
        String pk = String.valueOf(this.pk(eruptModel, from));
        if (pk.equals(targetPk)) return true;
        if (!seen.add(pk)) return false;
        Object node = eruptModel.getClazz().isInstance(from) ? from
                : DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, this.pk(eruptModel, from));
        if (null == node) return false;
        for (Object predecessor : this.predecessors(depField, node)) {
            if (this.reaches(eruptModel, depField, predecessor, targetPk, seen)) return true;
        }
        return false;
    }

    // a detached copy of the row before the change; a collection field is copied too, since the
    // managed entity's own collection is what gets mutated
    @SneakyThrows
    private Object snapshot(EruptModel eruptModel, Object obj, Field collectionField) {
        Object before = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        EruptUtil.copyEruptFields(eruptModel, obj, before);
        if (null != collectionField && Collection.class.isAssignableFrom(collectionField.getType())) {
            Collection<Object> deps = (Collection<Object>) collectionField.get(obj);
            collectionField.set(before, null == deps ? null : this.copy(collectionField, deps));
        }
        return before;
    }

    // a plain copy matching the declared field type, so reflection can assign it back
    private Collection<Object> copy(Field field, Collection<Object> source) {
        return Set.class.isAssignableFrom(field.getType()) ? new LinkedHashSet<>(source) : new ArrayList<>(source);
    }

    private void update(EruptModel eruptModel, Object obj, Object before) {
        String oldData;
        try {
            oldData = EruptUtil.toMaskedJson(eruptModel, before);
        } catch (Exception e) {
            oldData = null;
        }
        OldEntityTL.set(oldData);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
        DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));
        applicationEventPublisher.publishEvent(new EruptEditEvent<>(eruptModel.getClazz(), obj, before));
    }

    @Getter
    @Setter
    public static class GanttDateCommand {

        private String visCode;

        private Object pk;

        private String startDate;

        private String endDate;

    }

    @Getter
    @Setter
    public static class GanttLinksCommand {

        private String visCode;

        private List<Object> pks;

    }

    @Getter
    @Setter
    public static class GanttLinkCommand {

        private String visCode;

        private Object pk;

        private Object predecessorPk;

        private boolean remove;

    }

}
