package com.erupt.service;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.model.EruptModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

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
                eruptModel.setClazz(clazz);
                eruptModel.setErupt(clazz.getAnnotation(Erupt.class));
                List<Field> eruptFields = new ArrayList<>();
                for (Field field : clazz.getFields()) {
                    EruptField eruptField = field.getAnnotation(EruptField.class);
                    if (null != eruptField) {
                        eruptFields.add(field);
                    }
                }
                eruptModel.setEruptFields(eruptFields);
                ERUPTS.put(eruptName, eruptModel);
            }
        }
    }
}
