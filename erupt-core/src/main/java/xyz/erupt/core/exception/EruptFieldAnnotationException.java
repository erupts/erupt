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
                            throw ExceptionAnsi.styleEruptFieldException(eruptFieldModel, "@View注解" + view.title() + "必须指定column值");
                        }
                    }
                }
                break;
        }
    }
}
