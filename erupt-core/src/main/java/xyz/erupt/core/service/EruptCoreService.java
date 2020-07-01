package xyz.erupt.core.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.config.EruptConfig;
import xyz.erupt.core.exception.EruptAnnotationException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 9/28/18.
 */
@Order(1)
@Service
@Log
public final class EruptCoreService implements ApplicationRunner {

    @Autowired
    private EruptConfig eruptConfig;

    private static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<>();

    public static EruptModel getErupt(String eruptName) {
        return ERUPTS.get(eruptName);
    }

    public static EruptModel getEruptView(String eruptName) {
        EruptModel eruptModel = initEruptModel(ERUPTS.get(eruptName).getClazz(), true);
        ERUPTS.put(eruptName, eruptModel);
        return eruptModel;
    }

    private static EruptModel initEruptModel(Class clazz, boolean serialize) {
        //erupt domain info to memory
        EruptModel eruptModel = new EruptModel(clazz, serialize);
        // erupt field info to memory
        {
            List<EruptFieldModel> eruptFieldModels = new ArrayList<>();
            Map<String, EruptFieldModel> eruptFieldMap = new LinkedCaseInsensitiveMap<>();
            //erupt class annotation
            {
                try {
                    Object eruptObject = eruptModel.getClazz().newInstance();
                    ReflectUtil.findClassAllFields(clazz, field -> {
                        if (null != field.getAnnotation(EruptField.class)) {
                            try {
                                EruptFieldModel eruptFieldModel = new EruptFieldModel(field, serialize);
                                field.setAccessible(true);
                                eruptFieldModel.setValue(field.get(eruptObject));
                                eruptFieldModels.add(eruptFieldModel);
                                eruptFieldMap.put(field.getName(), eruptFieldModel);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            eruptModel.setEruptFieldModels(eruptFieldModels);
            eruptModel.setEruptFieldMap(eruptFieldMap);
        }
        EruptAnnotationException.validateEruptInfo(eruptModel);
        return eruptModel;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EruptSpringUtil.scannerPackage(eruptConfig.getScannerPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(Erupt.class)
        }, clazz -> {
            //other info to memory
            EruptModel eruptModel = initEruptModel(clazz, false);
            ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        });
        log.info("Erupt core initialization complete");
    }
}
