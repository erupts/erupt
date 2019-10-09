package xyz.erupt.core.bean;

import com.google.gson.JsonObject;
import lombok.Data;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liyuepeng on 10/10/18.
 */
@Data
public class EruptFieldModel {

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
        //数字类型转换
        if (TypeUtil.isNumberType(field.getType().getSimpleName())) {
            this.fieldReturnName = JavaType.NUMBER;
        } else {
            this.fieldReturnName = field.getType().getSimpleName();
        }
        //如果是Tab类型视图，数据必须为一对多关系管理，需要用泛型集合来存放，所以取出泛型的名称重新赋值到fieldReturnName中
        switch (eruptField.edit().type()) {
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
                this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
                break;
            case CHOICE:
                choiceMap = new HashMap<>();
                for (VL vl : eruptField.edit().choiceType().vl()) {
                    choiceMap.put(vl.value(), vl.label());
                }
                break;
            case DEPEND_SWITCH:
                choiceMap = new HashMap<>();
                for (DependSwitchType.Attr vl : eruptField.edit().dependSwitchType().attr()) {
                    choiceMap.put(vl.value(), vl.label());
                }
                break;
        }
        this.eruptAutoConfig();
        this.eruptFieldJson = AnnotationUtil.annotationToJsonByReflect(this.eruptField);
        //this.eruptFieldJson = new JsonParser().parse(AnnotationUtil.annotationToJson(eruptField.toString())).getAsJsonObject();
        //校验注解的正确性
        EruptFieldAnnotationException.validateEruptFieldInfo(this);
    }

    public static final String TYPE = "type";

    /**
     * erupt自动配置功能
     */
    private void eruptAutoConfig() {
        try {
            Map<String, Object> memberValues = getAnnotationMap(this.eruptField.edit());
            String type = memberValues.get(TYPE).toString();
            if (EditType.AUTO.name().equals(type)) {
                if (JavaType.BOOLEAN.equals(this.fieldReturnName.toLowerCase())) {
                    memberValues.put(TYPE, EditType.BOOLEAN);
                } else if (JavaType.DATE.equals(this.fieldReturnName)) {
                    memberValues.put(TYPE, EditType.DATE);
                } else if (JavaType.NUMBER.equals(this.fieldReturnName)) {
                    memberValues.put(TYPE, EditType.NUMBER);
                } else {
                    memberValues.put(TYPE, EditType.INPUT);
                }
                if (this.fieldName.toLowerCase().contains("img") || this.fieldName.toLowerCase().contains("image")) {
                    memberValues.put(TYPE, EditType.ATTACHMENT);
                    this.getAnnotationMap(this.eruptField.edit().attachmentType()).put(TYPE, AttachmentType.Type.IMAGE);
                } else if (this.fieldName.toLowerCase().contains("remark") || this.fieldName.toLowerCase().contains("desc")) {
                    memberValues.put(TYPE, EditType.TEXTAREA);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getAnnotationMap(Object obj) throws NoSuchFieldException, IllegalAccessException {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(obj);
        Field value = invocationHandler.getClass().getDeclaredField("memberValues");
        value.setAccessible(true);
        return (Map<String, Object>) value.get(invocationHandler);
    }
}
