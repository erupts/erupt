package xyz.erupt.core.exception;

import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.view.EruptFieldModel;

/**
 * @author YuePeng
 * date 11/1/18.
 */
public class EruptFieldAnnotationException extends RuntimeException {

    public EruptFieldAnnotationException(String message) {
        super(message);
    }

    public static void validateEruptFieldInfo(EruptFieldModel eruptFieldModel) {
        Edit edit = eruptFieldModel.getEruptField().edit();
        switch (edit.type()) {
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
                if (eruptFieldModel.getEruptField().views().length > 0) {
                    for (View view : eruptFieldModel.getEruptField().views()) {
                        if ("".equals(view.column())) {
                            throw ExceptionAnsi.styleEruptFieldException(eruptFieldModel, "@View注解修饰复杂对象，必须配置column值 → " + view.title());
                        }
                    }
                }
                break;
        }
    }
}
