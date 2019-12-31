package xyz.erupt.core.bean;

import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-10-10.
 */
@Data
public class EruptFieldModel {

    private transient EruptField eruptField;

    private transient Field field;

    private Map<String, String> choiceMap;

    private String fieldName;

    private String fieldReturnName;

    private JsonObject eruptFieldJson;

    private Object value;

    public EruptFieldModel(Field field) throws IllegalAccessException, InstantiationException {
        this.field = field;
        this.eruptField = field.getAnnotation(EruptField.class);
        Edit edit = eruptField.edit();
        this.fieldName = field.getName();
        //数字类型转换
        if (TypeUtil.isNumberType(field.getType().getSimpleName())) {
            this.fieldReturnName = JavaType.NUMBER;
        } else {
            this.fieldReturnName = field.getType().getSimpleName();
        }
        //如果是Tab类型视图，数据必须为一对多关系管理，需要用泛型集合来存放，所以取出泛型的名称重新赋值到fieldReturnName中
        switch (edit.type()) {
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
                this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
                break;
            case CHOICE:
                choiceMap = new HashMap<>();
                for (VL vl : edit.choiceType().vl()) {
                    choiceMap.put(vl.value(), vl.label());
                }
                for (Class<? extends ChoiceFetchHandler> cla : edit.choiceType().fetchHandler()) {
                    Map map = cla.newInstance().fetch(edit.choiceType().fetchHandlerParams());
                    if (null != map) {
                        choiceMap.putAll(cla.newInstance().fetch(edit.choiceType().fetchHandlerParams()));
                    }
                }
                break;
            case DEPEND_SWITCH:
                choiceMap = new HashMap<>();
                for (DependSwitchType.Attr vl : edit.dependSwitchType().attr()) {
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
            // view auto
//            for (View view : this.eruptField.views()) {
//                if (ViewType.AUTO == view.type()) {
//                    Map<String, Object> viewValues = getAnnotationMap(view);
//                    viewValues.put(TYPE, ViewType.TEXT);
//                }
//            }
            // edit auto
            if (!AnnotationConst.EMPTY_STR.equals(this.eruptField.edit().title()) && EditType.AUTO == this.eruptField.edit().type()) {
                Map<String, Object> editValues = getAnnotationMap(this.eruptField.edit());
                //根据返回类型推断
                if (JavaType.BOOLEAN.equals(this.fieldReturnName.toLowerCase())) {
                    editValues.put(TYPE, EditType.BOOLEAN);
                } else if (JavaType.DATE.equals(this.fieldReturnName)) {
                    editValues.put(TYPE, EditType.DATE);
                } else if (JavaType.NUMBER.equals(this.fieldReturnName)) {
                    editValues.put(TYPE, EditType.NUMBER);
                } else {
                    editValues.put(TYPE, EditType.INPUT);
                }
                //根据属性名推断
                if (ArrayUtils.contains(AnnotationUtil.getEditTypeMapping(EditType.TEXTAREA).nameInfer(), this.fieldName.toLowerCase())) {
                    editValues.put(TYPE, EditType.TEXTAREA);
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
