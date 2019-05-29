package xyz.erupt.core.model;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * Created by liyuepeng on 2019-05-29.
 */
@Data
public class EruptFieldAndValue {
    private Field field;

    private Object fieldValue;

    public EruptFieldAndValue(Field field, Object fieldValue) {
        this.field = field;
        this.fieldValue = fieldValue;
    }
}
