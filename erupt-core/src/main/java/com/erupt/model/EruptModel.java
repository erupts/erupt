package com.erupt.model;

import com.erupt.annotation.Erupt;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by liyuepeng on 9/29/18.
 */
public class EruptModel {

    private Class<?> clazz;

    private Erupt erupt;

    private List<Field> eruptFields;


    public Erupt getErupt() {
        return erupt;
    }

    public void setErupt(Erupt erupt) {
        this.erupt = erupt;
    }

    public List<Field> getEruptFields() {
        return eruptFields;
    }

    public void setEruptFields(List<Field> eruptFields) {
        this.eruptFields = eruptFields;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }


}
