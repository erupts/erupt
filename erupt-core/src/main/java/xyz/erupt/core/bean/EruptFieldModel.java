package xyz.erupt.core.bean;

import com.google.gson.JsonObject;
import lombok.Data;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Data
public class EruptFieldModel {

    public static final String NUMBER_TYPE = "number";

    private transient EruptField eruptField;

    private transient Field field;

    private transient Map<String, String> choiceMap;

    private String fieldName;

    private String fieldReturnName;

    private JsonObject eruptFieldJson;

    private Object value;

    public EruptFieldModel(Field field) {
        this.field = field;
        this.eruptField = field.getAnnotation(EruptField.class);
        this.fieldName = field.getName();
        if (TypeUtil.isNumberType(field.getType().getSimpleName())) {
            this.fieldReturnName = NUMBER_TYPE;
        } else {
            this.fieldReturnName = field.getType().getSimpleName();
        }
        //如果是Tab类型视图，数据必须为一对多关系管理，需要用泛型集合来存放，固！取出泛型的名称重新赋值到fieldReturnName中
        if (eruptField.edit().type() == EditType.TAB_TREE
                || eruptField.edit().type() == EditType.TAB_TABLE_ADD
                || eruptField.edit().type() == EditType.TAB_TABLE_REFER) {
            this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
        } else if (eruptField.edit().type() == EditType.CHOICE) {
            choiceMap = new HashMap<>();
            for (VL vl : eruptField.edit().choiceType().vl()) {
                choiceMap.put(vl.value(), vl.label());
            }
        }
        this.eruptFieldJson = AnnotationUtil.annotationToJsonByReflect(this.eruptField);
        //this.eruptFieldJson = new JsonParser().parse(AnnotationUtil.annotationToJson(eruptField.toString())).getAsJsonObject();
        //校验注解的正确性
        EruptFieldAnnotationException.validateEruptFieldInfo(this);
    }
}
