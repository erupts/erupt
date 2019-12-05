package xyz.erupt.core.exception;

import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.bean.EruptFieldModel;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptFieldAnnotationException extends RuntimeException {

    public EruptFieldAnnotationException(String message) {
        super(message);
    }

    public static void validateEruptFieldInfo(EruptFieldModel eruptFieldModel) {
        switch (eruptFieldModel.getEruptField().edit().type()) {
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
                if (eruptFieldModel.getEruptField().views().length > 0) {
                    for (View view : eruptFieldModel.getEruptField().views()) {
                        if ("".equals(view.column())) {
                            throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "@View注解" + view.title() + "必须指定column值");
                        }
                    }
                }
                break;
        }


    }
}
