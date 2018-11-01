package com.erupt.exception;

import com.erupt.constant.EruptConst;
import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptFieldAnnotationException extends RuntimeException {

    public EruptFieldAnnotationException(String message) {
        super(message);
    }


    public static void validateEruptFieldInfo(EruptFieldModel eruptFieldModel) {
        String declaringClass = eruptFieldModel.getField().getDeclaringClass().getName();
        switch (eruptFieldModel.getEruptField().edit().type()) {
            case DICT:
                if (eruptFieldModel.getEruptField().edit().dictType().length == 0) {
                    ExceptionUtil.styleEruptException(eruptFieldModel, "字典类型未使用@DictType注解修饰");
                }
                break;
            case CHOICE:
                if (eruptFieldModel.getEruptField().edit().choiceType().length == 0) {
                    ExceptionUtil.styleEruptException(eruptFieldModel, "字典类型未使用@DictType注解修饰");
                }
                break;
            case REFERENCE:
                if (eruptFieldModel.getEruptField().edit().referenceType().length == 0) {
                    ExceptionUtil.styleEruptException(eruptFieldModel, "引用类型未使用@ReferenceType注解修饰");
                }
                break;
            case BOOLEAN:

                break;
        }
    }
}
