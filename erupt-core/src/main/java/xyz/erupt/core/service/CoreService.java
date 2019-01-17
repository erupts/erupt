package xyz.erupt.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.exception.EruptAnnotationException;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.util.ScannerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Service
public class CoreService implements InitializingBean {


    @Value("#{'${erupt.scanner-package}'.split(',')}")
    private String[] packages;

    public static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<>();


    @Override
    public void afterPropertiesSet() {
        ScannerUtil.scannerPackage(this.packages, new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, clazz -> {
            //erupt domain info to memory
            EruptModel eruptModel = new EruptModel(clazz);
            // erupt field info to memory
            {
                List<EruptFieldModel> eruptFieldModels = new ArrayList<>();
                Map<String, EruptFieldModel> eruptFieldMap = new LinkedCaseInsensitiveMap<EruptFieldModel>();
                //erupt class annotation
                {
                    Class tempClass = clazz;
                    while (null != tempClass) {
                        for (Field field : tempClass.getDeclaredFields()) {
                            EruptField eruptField = field.getAnnotation(EruptField.class);
                            if (null != eruptField) {
                                EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
                                eruptFieldModels.add(eruptFieldModel);
                                eruptFieldMap.put(field.getName(), eruptFieldModel);
                            }
                        }
                        tempClass = tempClass.getSuperclass();
                    }
                }
                eruptModel.setEruptFieldModels(eruptFieldModels);
                eruptModel.setEruptFieldMap(eruptFieldMap);
            }
            EruptAnnotationException.validateEruptInfo(eruptModel);
            //other info to memory
            ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        });
    }


}
