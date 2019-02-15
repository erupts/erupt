package xyz.erupt.core.model;

import lombok.Data;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.util.ConfigUtil;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.erupt.core.exception.ExceptionUtil;
import xyz.erupt.core.util.ReflectUtil;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Data
public class EruptFieldModel implements Serializable {

    private transient EruptField eruptField;

    private transient Field field;

    private EruptModel eruptModel;

    private JsonObject eruptFieldJson;

    private String fieldName;

    private String fieldReturnName;

    private Object value;

    public EruptFieldModel(Field field) {
        this.field = field;
        this.eruptField = field.getAnnotation(EruptField.class);
        this.fieldName = field.getName();
        this.fieldReturnName = field.getType().getSimpleName();
        //如果是Tab类型视图，数据必须为一对多关系管理，需要用泛型集合来存放，固！取出泛型的名称重新赋值到fieldReturnName中
        if (eruptField.edit().type() == EditType.TAB) {
            this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
        }
        try {
            this.eruptFieldJson = new JsonParser().parse(ConfigUtil.annoStrToJsonStr(eruptField.toString())).getAsJsonObject();
        } catch (Exception e) {
            throw ExceptionUtil.styleEruptFieldException(this, ExceptionUtil.ANNOTATION_PARSE_ERR_STR);
        }
        //校验注解的正确性
        EruptFieldAnnotationException.validateEruptFieldInfo(this);
    }
}
