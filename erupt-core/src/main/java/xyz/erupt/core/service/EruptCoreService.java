package xyz.erupt.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        return ERUPTS.get(eruptName);
    }

    //动态注册erupt类
    public static void registerErupt(Class<?> eruptClazz) {
        if (ERUPTS.containsKey(eruptClazz.getSimpleName())) {
            throw new RuntimeException(eruptClazz.getSimpleName() + " conflict !");
        }
        EruptModel eruptModel = initEruptModel(eruptClazz);
        ERUPTS.put(eruptClazz.getSimpleName(), eruptModel);
        ERUPT_LIST.add(eruptModel);
    }

    //移除容器内所维护的Erupt
    public static void unregisterErupt(Class<?> eruptClazz) {
        ERUPTS.remove(eruptClazz.getSimpleName());
        ERUPT_LIST.removeIf(model -> model.getEruptName().equals(eruptClazz.getSimpleName()));
    }

    @SneakyThrows
    public static EruptModel getEruptView(String eruptName) {
        EruptModel em = getErupt(eruptName).clone();
        for (EruptFieldModel fieldModel : em.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.type() == EditType.CHOICE) {
                fieldModel.setComponentValue(EruptUtil.getChoiceList(em, edit.choiceType()));
            } else if (edit.type() == EditType.TAGS) {
                fieldModel.setComponentValue(EruptUtil.getTagList(edit.tagsType()));
            }
        }
        return em;
    }

    private static EruptModel initEruptModel(Class<?> clazz) {
        // erupt class data to memory
        EruptModel eruptModel = new EruptModel(clazz);
        // erupt field data to memory
        eruptModel.setEruptFieldModels(new ArrayList<>());
        eruptModel.setEruptFieldMap(new LinkedCaseInsensitiveMap<>());
        ReflectUtil.findClassAllFields(clazz, field -> Optional.ofNullable(field.getAnnotation(EruptField.class))
                .ifPresent(ignore -> {
                    EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
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
            EruptModel eruptModel = initEruptModel(clazz);
            ERUPTS.put(clazz.getSimpleName(), eruptModel);
            ERUPT_LIST.add(eruptModel);
        });
        log.info("<" + repeat("===", 20) + ">");
        AtomicInteger moduleMaxCharLength = new AtomicInteger();
        EruptModuleInvoke.invoke(it -> {
            int length = it.info().getName().length();
            if (length > moduleMaxCharLength.get()) moduleMaxCharLength.set(length);
        });
        EruptModuleInvoke.invoke(it -> {
            it.run();
            MODULES.add(it.info().getName());
            log.info("🚀 -> {} module initialization completed in {}ms", fillCharacter(it.info().getName(),
                    moduleMaxCharLength.get()), timeRecorder.recorder()
            );
        });
        log.info("Erupt modules : " + MODULES.size());
        log.info("Erupt classes : " + ERUPTS.size());
        log.info("Erupt Framework initialization completed in {}ms", totalRecorder.recorder());
        log.info("<" + repeat("===", 20) + ">");
    }

    private String fillCharacter(String character, int targetWidth) {
        return character + repeat(" ", targetWidth - character.length());
    }

    private String repeat(String space, int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) sb.append(space);
        return sb.toString();
    }

}
