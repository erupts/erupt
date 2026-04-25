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
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.exception.EruptAnnotationException;
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

    private static final List<String> MODULES = new ArrayList<>();

    public static List<String> getModules() {
        return MODULES;
    }

    public static List<EruptModel> getErupts() {
        return ERUPT_LIST;
    }

    public static EruptModel getErupt(String eruptName) {
        if (EruptSpringUtil.getBean(EruptProp.class).isHotBuild()) {
            if (null == ERUPTS.get(eruptName)) {
                return ERUPTS.get(eruptName);
            } else {
                return EruptCoreService.initEruptModel(ERUPTS.get(eruptName).getClazz(), false);
            }
        } else {
            return ERUPTS.get(eruptName);
        }
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
        EruptModel em = getErupt(eruptName).clone();
        for (EruptFieldModel fieldModel : em.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.type() == EditType.CHOICE || edit.type() == EditType.MULTI_CHOICE) {
                fieldModel.setComponentValue(EruptUtil.getChoiceList(em, edit));
            } else if (edit.type() == EditType.TAGS) {
                fieldModel.setComponentValue(EruptUtil.getTagList(edit.tagsType()));
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
