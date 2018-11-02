package com.erupt.service;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.EditType;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.erupt.util.EruptAnnoConver;
import com.erupt.util.ScannerUtil;
import com.google.gson.JsonParser;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
                //super class erupt annotation
                {
                    Class<?> superClass = clazz.getSuperclass();
                    if (null != clazz.getSuperclass()) {
                        for (Field field : superClass.getDeclaredFields()) {
                            EruptField eruptField = field.getAnnotation(EruptField.class);
                            eruptModel.setPrimaryKeyCol(field);
                            if (null != eruptField) {
                                EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
                                eruptFieldModels.add(eruptFieldModel);
                                eruptFieldMap.put(field.getName(), eruptFieldModel);
                            }
                        }
                    }

                }
                for (Field field : clazz.getDeclaredFields()) {
                    EruptField eruptField = field.getAnnotation(EruptField.class);
                    eruptModel.setPrimaryKeyCol(field);
                    if (null != eruptField) {
                        EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
                        eruptFieldModels.add(eruptFieldModel);
                        eruptFieldMap.put(field.getName(), eruptFieldModel);
                    }
                }
                eruptModel.setEruptFieldModels(eruptFieldModels);
                eruptModel.setEruptFieldMap(eruptFieldMap);
            }
            System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(eruptModel.getEruptJson().toString()));
            //other info to memory
            ERUPTS.put(eruptModel.getEruptName(), eruptModel);
        });
    }


}
