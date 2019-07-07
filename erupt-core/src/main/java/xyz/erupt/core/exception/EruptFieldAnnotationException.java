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
            case CHOICE:
//                if (eruptFieldModel.getEruptField().edit().choiceType().length == 0) {
//                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "选择类型未使用@ChoiceType注解修饰");
//                }
//                String typeName = eruptFieldModel.getField().getType().getSimpleName();
//                if (!"int".equals(typeName) && !"Integer".equals(typeName)) {
//                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "@ChoiceType类型返回值必须使用Integer类型接收");
//                }
                break;
            case REFERENCE_TREE:
//                if (eruptFieldModel.getEruptField().edit().referenceTreeType().length == 0) {
//                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "引用类型未使用@ReferenceType注解修饰");
//                }
                if (eruptFieldModel.getEruptField().views().length > 0) {
                    for (View view : eruptFieldModel.getEruptField().views()) {
                        if ("".equals(view.column())) {
                            throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "引用类型@view注解未指定column值");
                        }
                    }
                }
                break;
//            case CUSTOM_REFER:
//                if (eruptFieldModel.getEruptField().edit().customReferType().length == 0) {
//                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "自定义引用类型未使用@CustomReferType注解修饰");
//                }
//                break;
            case BOOLEAN:
                if (!eruptFieldModel.getField().getType().getSimpleName().toLowerCase().equals("boolean")) {
                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "使用@BoolType注解，要求返回值必须为boolean类型");
                }
                break;
            case ATTACHMENT:
//                if (eruptFieldModel.getEruptField().edit().attachmentType().length == 0) {
//                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "附件类型未使用@AttachmentType注解修饰");
//                }
                break;
        }


    }
}
