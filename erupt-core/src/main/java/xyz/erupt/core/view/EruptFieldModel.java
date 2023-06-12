package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.exception.ExceptionAnsi;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.EruptFieldProxy;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.util.CloneSupport;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author YuePeng
 * date 2018-10-10.
 */
@Getter
@Setter
public class EruptFieldModel extends CloneSupport<EruptFieldModel> {

    private transient EruptField eruptField;

    private transient Field field;

    private transient String fieldReturnName;

    private transient AnnotationProxy<EruptField, Void> eruptFieldAnnotationProxy = new EruptFieldProxy();

    private String fieldName;

    private JsonObject eruptFieldJson;

    private Object value;

    private Object componentValue;

    public EruptFieldModel(Field field) {
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
            case CHECKBOX:
                try {
                    this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
                } catch (Exception e) {
                    throw ExceptionAnsi.styleEruptFieldException(this, "Component modification field is incorrect");
                }
                break;
        }
        this.eruptField = eruptFieldAnnotationProxy.newProxy(this.getEruptField());
        //校验注解的正确性
        EruptFieldAnnotationException.validateEruptFieldInfo(this);
    }

    public EruptField getEruptField() {
        ProxyContext.set(field);
        return eruptField;
    }

    public void serializable() {
        this.eruptFieldJson = AnnotationProcess.annotationToJsonByReflect(this.getEruptField());
    }

}
