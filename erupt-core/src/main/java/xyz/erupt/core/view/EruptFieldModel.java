package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.DependSwitchType;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
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

    private transient String fieldReturnName;

    private String fieldName;

    private Map<String, String> choiceMap;

    private JsonObject eruptFieldJson;

    private Object value;

    public EruptFieldModel(Field field, boolean serialize) {
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
        switch (edit.type()) {
            //如果是Tab类型视图，数据必须为一对多关系管理，需要用泛型集合来存放，所以取出泛型的名称重新赋值到fieldReturnName中
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
                this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
                break;
            case DEPEND_SWITCH:
                choiceMap = new HashMap<>();
                for (DependSwitchType.Attr vl : edit.dependSwitchType().attr()) {
                    choiceMap.put(vl.value(), vl.label());
                }
                break;
            case CHOICE:
                choiceMap = new HashMap<>();
                this.choiceMap = EruptUtil.getChoiceMap(edit.choiceType());
                break;
            default:
                break;
        }
        if (serialize) {
            this.eruptFieldJson = AnnotationUtil.annotationToJsonByReflect(this.eruptField);
        } else {
            this.eruptAutoConfig();
        }
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
            for (View view : this.eruptField.views()) {
                if (ViewType.AUTO == view.type()) {
                    Map<String, Object> viewValues = getAnnotationMap(view);
                    if (!AnnotationConst.EMPTY_STR.equals(this.eruptField.edit().title())) {
                        switch (this.eruptField.edit().type()) {
                            case ATTACHMENT:
                                if (this.eruptField.edit().attachmentType().type() == AttachmentType.Type.IMAGE) {
                                    viewValues.put(TYPE, ViewType.IMAGE);
                                } else {
                                    viewValues.put(TYPE, ViewType.ATTACHMENT);
                                }
                                continue;
                            case HTML_EDIT:
                                viewValues.put(TYPE, ViewType.HTML);
                                continue;
                        }
                    }
                    if (JavaType.BOOLEAN.equals(this.fieldReturnName.toLowerCase())) {
                        viewValues.put(TYPE, ViewType.BOOLEAN);
                    } else if (JavaType.DATE.equals(this.fieldReturnName)) {
                        viewValues.put(TYPE, ViewType.DATE);
                    } else if (JavaType.NUMBER.equals(this.fieldReturnName)) {
                        viewValues.put(TYPE, ViewType.NUMBER);
                    } else {
                        viewValues.put(TYPE, ViewType.TEXT);
                    }
                }
            }
            // edit auto
            if (StringUtils.isNotBlank(this.eruptField.edit().title()) && EditType.AUTO == this.eruptField.edit().type()) {
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
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
