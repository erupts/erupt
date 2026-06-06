package xyz.erupt.core.view;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
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

/**
 * @author YuePeng
 * date 2018-10-10.
 */
@Getter
@Setter
public class EruptFieldModel extends CloneSupport<EruptFieldModel> {

    private transient EruptField eruptField;

    private transient Field field;

    private transient AnnotationProxy<EruptField, Void> eruptFieldAnnotationProxy = new EruptFieldProxy();

    private transient boolean starting;

    private String fieldName;

    private String fieldReturnName;

    private JsonObject eruptFieldJson;

    private Object value;

    private Object componentValue;

    public static final String NUMBER = "number";

    public EruptFieldModel(Field field, boolean starting) {
        this.field = field;
        this.eruptField = field.getAnnotation(EruptField.class);
        Edit edit = eruptField.edit();
        this.fieldName = field.getName();
        // Numeric type conversion
        if (TypeUtil.isNumberType(field.getType().getSimpleName())) {
            this.fieldReturnName = NUMBER;
        } else {
            this.fieldReturnName = field.getType().getSimpleName();
        }
        switch (edit.type()) {
            // For Tab-type views, data must be managed as a one-to-many relationship using a generic collection, so the generic type name is extracted and re-assigned to fieldReturnName
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
        this.starting = starting;
        this.eruptField = eruptFieldAnnotationProxy.newProxy(this.getEruptField());
        // Validate the correctness of the annotation
        EruptFieldAnnotationException.validateEruptFieldInfo(this);
        this.starting = false;
    }

    public EruptField getEruptField() {
        ProxyContext.set(field, this.starting);
        return eruptField;
    }

    public void serializable() {
        this.eruptFieldJson = AnnotationProcess.annotationToJsonByReflect(this.getEruptField());
    }

}
