package com.erupt.service;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.erupt.EruptAnnoConver;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

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

    public static final Map<String, EruptModel> ERUPTS = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Erupt.class));
        for (String pack : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(pack)) {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                String eruptName = clazz.getSimpleName().toLowerCase();
                EruptModel eruptModel = new EruptModel();
                //erupt domain info to memory
                {
                    Erupt erupe = clazz.getAnnotation(Erupt.class);
                    eruptModel.setClazz(clazz);
                    eruptModel.setErupt(erupe);
                    eruptModel.setEruptJson(new JSONObject(ConfigUtil.annoStrToJsonStr(erupe.toString())));
                }
                // erupt field info to memory
                {
                    List<EruptFieldModel> eruptFieldModels = new ArrayList<>();
                    for (Field field : clazz.getFields()) {
                        EruptField eruptField = field.getAnnotation(EruptField.class);
                        if (null != eruptField) {
                            EruptFieldModel eruptFieldModel = new EruptFieldModel();
                            eruptFieldModel.setEruptField(eruptField);
                            eruptFieldModel.setField(field);
                            eruptFieldModels.add(eruptFieldModel);
                            System.out.println(eruptFieldModel.getEruptFieldJson());
                            EruptAnnoConver.validateEruptFieldInfo(eruptFieldModel);
                        }

                    }
                    eruptModel.setEruptFieldModels(eruptFieldModels);
                }

                EruptAnnoConver.shakeViewToLineView(eruptModel);

                System.out.println(eruptModel.getEruptViews().toString());
                ERUPTS.put(eruptName, eruptModel);
            }
        }
    }


    public EruptModel getEruptModel(String eruptName) {
        return CoreService.ERUPTS.get(eruptName);
    }


}
