package xyz.erupt.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.Ansi;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.exception.EruptAnnotationException;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * @author YuePeng
 * date 9/28/18.
 */
@Order(100)
@Service
@Slf4j
public class EruptCoreService implements ApplicationRunner {

    private static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<>();

    private static final List<EruptModel> ERUPT_LIST = new ArrayList<>();

    // Erupts registered at runtime by model instance (e.g. erupt-designer), not rebuildable from class
    private static final Set<String> RUNTIME_ERUPTS = new HashSet<>();

    private static final List<String> MODULES = new ArrayList<>();

    public static List<String> getModules() {
        return MODULES;
    }

    public static List<EruptModel> getErupts() {
        return ERUPT_LIST;
    }

    public static boolean isRuntimeErupt(String eruptName) {
        return RUNTIME_ERUPTS.contains(eruptName.toLowerCase());
    }

    public static EruptModel getErupt(String eruptName) {
        if (EruptSpringUtil.getBean(EruptProp.class).isHotBuild()) {
            if (null == ERUPTS.get(eruptName) || RUNTIME_ERUPTS.contains(eruptName.toLowerCase())) {
                return ERUPTS.get(eruptName);
            } else {
                return EruptCoreService.initEruptModel(ERUPTS.get(eruptName).getClazz(), false);
            }
        } else {
            return ERUPTS.get(eruptName);
        }
    }

    /**
     * Like {@link #getErupt}, but falls back to a remote (erupt-cloud node) placeholder when the
     * erupt is not registered locally. Only call this from entry points that explicitly support
     * remote routing (the returned placeholder has no local class or field metadata).
     */
    public static EruptModel getEruptWithRemote(String eruptName) {
        EruptModel eruptModel = getErupt(eruptName);
        if (null == eruptModel && EruptRemoteRouterManager.isRemote(eruptName)) {
            return EruptRemoteRouterManager.get().resolveErupt(eruptName);
        }
        return eruptModel;
    }

    /**
     * Whether {@code eruptName} is reachable from {@code parentModel} through nested structures:
     * fields whose return type is a registered erupt (tab / multi-form / combine / reference)
     * and rowOperation forms. The security layer uses this to authorize sub-erupt requests
     * against the menu-bound ancestor erupt, so nesting depth is not limited to one level.
     */
    public static boolean isEruptNested(EruptModel parentModel, String eruptName) {
        Set<String> visited = new HashSet<>();
        Deque<EruptModel> queue = new ArrayDeque<>();
        visited.add(parentModel.getEruptName().toLowerCase());
        queue.add(parentModel);
        while (!queue.isEmpty()) {
            EruptModel node = queue.poll();
            for (EruptFieldModel fieldModel : node.getEruptFieldModels()) {
                if (nestedMatchOrEnqueue(fieldModel.getFieldReturnName(), eruptName, visited, queue)) return true;
            }
            for (RowOperation operation : node.getErupt().rowOperation()) {
                if (void.class != operation.eruptClass()
                        && nestedMatchOrEnqueue(operation.eruptClass().getSimpleName(), eruptName, visited, queue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean nestedMatchOrEnqueue(String name, String target, Set<String> visited, Deque<EruptModel> queue) {
        EruptModel model = getErupt(name);
        if (null == model || !visited.add(model.getEruptName().toLowerCase())) return false;
        if (model.getEruptName().equalsIgnoreCase(target)) return true;
        queue.add(model);
        return false;
    }

    // Dynamically register a prebuilt erupt model (replace if exists), effective without restart
    public static void registerErupt(EruptModel eruptModel) {
        unregisterErupt(eruptModel.getEruptName());
        ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        ERUPT_LIST.add(eruptModel);
        RUNTIME_ERUPTS.add(eruptModel.getEruptName().toLowerCase());
    }

    // Remove a runtime-registered erupt by name
    public static void unregisterErupt(String eruptName) {
        ERUPTS.remove(eruptName);
        ERUPT_LIST.removeIf(model -> model.getEruptName().equalsIgnoreCase(eruptName));
        RUNTIME_ERUPTS.remove(eruptName.toLowerCase());
    }

    // Dynamically register an erupt class
    public static void registerErupt(Class<?> eruptClazz) {
        if (ERUPTS.containsKey(eruptClazz.getSimpleName())) {
            throw new RuntimeException(eruptClazz.getSimpleName() + " conflict !");
        }
        EruptModel eruptModel = initEruptModel(eruptClazz, true);
        ERUPTS.put(eruptClazz.getSimpleName(), eruptModel);
        ERUPT_LIST.add(eruptModel);
    }

    // Remove the Erupt maintained in the container
    public static void unregisterErupt(Class<?> eruptClazz) {
        ERUPTS.remove(eruptClazz.getSimpleName());
        ERUPT_LIST.removeIf(model -> model.getEruptName().equals(eruptClazz.getSimpleName()));
    }

    @SneakyThrows
    public static EruptModel getEruptView(String eruptName) {
        // erupt-cloud: remote erupts have no local class to clone; return the node's fetched schema
        if (EruptRemoteRouterManager.isRemote(eruptName)) {
            return EruptRemoteRouterManager.get().resolveEruptView(eruptName);
        }
        EruptModel em = getErupt(eruptName).clone();
        for (EruptFieldModel fieldModel : em.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.type() == EditType.CHOICE || edit.type() == EditType.MULTI_CHOICE) {
                fieldModel.setComponentValue(EruptUtil.getChoiceList(em, edit));
            }else if (edit.type() == EditType.TAGS){
                fieldModel.setComponentValue(edit.tagsType().tags());
            }
        }
        return em;
    }

    private static EruptModel initEruptModel(Class<?> clazz, boolean starting) {
        // erupt class data to memory
        EruptModel eruptModel = new EruptModel(clazz);
        // erupt field data to memory
        eruptModel.setEruptFieldModels(new ArrayList<>());
        eruptModel.setEruptFieldMap(new LinkedCaseInsensitiveMap<>());
        ReflectUtil.findClassAllFields(clazz, field -> Optional.ofNullable(field.getAnnotation(EruptField.class))
                .ifPresent(ignore -> {
                    EruptFieldModel eruptFieldModel = new EruptFieldModel(field, starting);
                    eruptModel.getEruptFieldModels().add(eruptFieldModel);
                    if (!eruptModel.getEruptFieldMap().containsKey(field.getName())) {
                        eruptModel.getEruptFieldMap().put(field.getName(), eruptFieldModel);
                    }
                })
        );
        eruptModel.getEruptFieldModels().sort(Comparator.comparingInt((a) -> a.getEruptField().sort()));
        // erupt annotation validate
        EruptAnnotationException.validateEruptInfo(eruptModel);
        return eruptModel;
    }

    @Override
    public void run(ApplicationArguments args) {
        TimeRecorder totalRecorder = new TimeRecorder();
        TimeRecorder timeRecorder = new TimeRecorder();
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(Erupt.class)
        }, clazz -> {
            EruptModel eruptModel = initEruptModel(clazz, true);
            ERUPTS.put(clazz.getSimpleName(), eruptModel);
            ERUPT_LIST.add(eruptModel);
        });
        AtomicInteger moduleMaxCharLength = new AtomicInteger();
        EruptModuleInvoke.invoke(it -> {
            int len = it.info().getName().length();
            if (len > moduleMaxCharLength.get()) moduleMaxCharLength.set(len);
        });
        String sep = ansi().fgBright(Ansi.Color.BLACK).a("─".repeat(54)).reset().toString();
        log.info(sep);
        if (EruptSpringUtil.getBean(EruptProp.class).isHotBuild()) {
            log.warn(ansi().fg(Ansi.Color.RED).a("  ⚠ Hot build enabled").reset().toString());
        }
        EruptModuleInvoke.invoke(it -> {
            it.run();
            MODULES.add(it.info().getName());
            log.info("  {} {}", ansi().fgBright(Ansi.Color.CYAN).a(fillCharacter(it.info().getName(), moduleMaxCharLength.get())).reset(),
                    ansi().fgBright(Ansi.Color.BLACK).a(timeRecorder.recorder() + "ms").reset());
        });
        log.info(sep);
        log.info("  {}{}   {}{}   {}{}",
                ansi().fgBright(Ansi.Color.BLACK).a("Modules  ").reset(), MODULES.size(),
                ansi().fgBright(Ansi.Color.BLACK).a("Classes  ").reset(), ERUPTS.size(),
                ansi().fgBright(Ansi.Color.BLACK).a("Ready in  ").reset(),
                ansi().fgBright(Ansi.Color.GREEN).a(totalRecorder.recorder() + "ms").reset());
        log.info(sep);
    }

    private String fillCharacter(String character, int targetWidth) {
        return character + " ".repeat(targetWidth - character.length());
    }

}
