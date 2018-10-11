package com.erupt.service;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.util.ConfigUtil;
import com.erupt.erupt.EruptAnnoConver;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import com.erupt.util.ScannerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by liyuepeng on 9/28/18.
 */
@Service
public class CoreService implements InitializingBean {


    @Value("#{'${erupt.scanner-package}'.split(',')}")
    public List<String> packages;

    public static final Map<String, EruptModel> ERUPTS = new HashMap<>();

    private Gson gson = new Gson();

    @Override
    public void afterPropertiesSet() throws Exception {
        ScannerUtil.scannerPackage(packages, new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, clazz -> {
            String eruptName = clazz.getSimpleName().toLowerCase();
            EruptModel eruptModel = new EruptModel();
            //erupt domain info to memory
            {
                Erupt erupt = clazz.getAnnotation(Erupt.class);
                eruptModel.setClazz(clazz);
                eruptModel.setErupt(erupt);
                System.out.println(Ansi.ansi().bg(Ansi.Color.RED).a(ConfigUtil.annoStrToJsonStr(erupt.toString())));
                System.out.println(new JsonParser().parse(ConfigUtil.annoStrToJsonStr(erupt.toString())).getAsJsonObject());
                eruptModel.setEruptJson(new JsonParser().parse(ConfigUtil.annoStrToJsonStr(erupt.toString())).getAsJsonObject());
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
                        EruptAnnoConver.validateEruptFieldInfo(eruptFieldModel);
                    }
                }
                eruptModel.setEruptFieldModels(eruptFieldModels);
            }
            //other info to memory
            EruptAnnoConver.gcEruptFieldViews(eruptModel);
            System.out.println(eruptModel.getEruptJson());
            ERUPTS.put(eruptName, eruptModel);
        });
    }


    public EruptModel getEruptModel(String eruptName) {
        return CoreService.ERUPTS.get(eruptName);
    }


}
