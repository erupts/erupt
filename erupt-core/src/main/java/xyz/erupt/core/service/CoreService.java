package xyz.erupt.core.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
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
 * Created by liyuepeng on 9/28/18.
 */
@Service
public class CoreService implements InitializingBean {

    @Autowired
    private EruptConfig eruptConfig;

    private static boolean staticHotBuild;

    private static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<>();

    public static EruptModel getErupt(String eruptName) {
        if (staticHotBuild) {
            EruptModel eruptModel = ERUPTS.get(eruptName);
            if (null != eruptModel) {
                return initEruptModel(eruptModel.getClazz());
            } else {
                return null;
            }
        } else {
            return ERUPTS.get(eruptName);
        }
    }

    @Override
    public void afterPropertiesSet() {
        CoreService.staticHotBuild = eruptConfig.isHotBuild();
        new EruptSpringUtil().scannerPackage(eruptConfig.getScannerPackage(), new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, clazz -> {
            EruptModel eruptModel = initEruptModel(clazz);
            //other info to memory
            ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        });
    }

    private static EruptModel initEruptModel(Class clazz) {
        //erupt domain info to memory
        EruptModel eruptModel = new EruptModel(clazz);
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

}
