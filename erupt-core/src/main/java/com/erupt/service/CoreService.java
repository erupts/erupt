package com.erupt.service;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.exception.EruptAnnotationException;
import com.erupt.core.model.EruptFieldModel;
import com.erupt.core.model.EruptModel;
import com.erupt.util.ScannerUtil;
import org.fusesource.jansi.Ansi;
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
    public List<String> packages;


    public static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<EruptModel>();


    @Override
    public void afterPropertiesSet() throws Exception {
        ScannerUtil.scannerPackage(packages, new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, clazz -> {
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
                                eruptModel.setPrimaryKeyCol(field);
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
//            System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(eruptModel.getEruptJson().toString()));
            EruptAnnotationException.validateEruptInfo(eruptModel);
            //other info to memory
            ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        });
    }


}
