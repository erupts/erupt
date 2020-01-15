package xyz.erupt.core.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.config.EruptConfig;
import xyz.erupt.core.exception.EruptAnnotationException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 9/28/18.
 */
@Service
public class CoreService implements InitializingBean {

    @Autowired
    private EruptConfig eruptConfig;

    private static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<>();

    private static final Map<String, Class> ERUPT_CLASSES = new LinkedCaseInsensitiveMap<>();

    public static EruptModel getErupt(String eruptName) {
        Class clazz = ERUPT_CLASSES.get(eruptName);
        if (null != clazz) {
            return initEruptModel(clazz);
        }
        return null;
    }

    private static EruptModel initEruptModel(Class clazz) {
        //erupt domain info to memory
        EruptModel eruptModel = new EruptModel(clazz);
        try {
            Power power = eruptModel.getErupt().power();
            if (!power.powerHandler().isInterface()) {
                eruptModel.setAlwaysRebuild(true);
                PowerHandler powerHandler = power.powerHandler().newInstance();
                PowerHandler.PowerBean powerBean = new PowerHandler.PowerBean(power);
                powerHandler.handler(powerBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                                EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
                                field.setAccessible(true);
                                eruptFieldModel.setValue(field.get(eruptObject));
                                eruptFieldModels.add(eruptFieldModel);
                                eruptFieldMap.put(field.getName(), eruptFieldModel);
                            } catch (IllegalAccessException | InstantiationException e) {
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
    public void afterPropertiesSet() {
        EruptSpringUtil.scannerPackage(eruptConfig.getScannerPackage(), new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, clazz -> {
            ERUPT_CLASSES.put(clazz.getSimpleName(), clazz);
//            EruptModel eruptModel = initEruptModel(clazz, true);
//            //other info to memory
//            ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        });

    }

}
