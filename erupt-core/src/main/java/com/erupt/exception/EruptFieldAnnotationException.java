package com.erupt.exception;

import com.erupt.annotation.sub_field.View;
import com.erupt.base.model.EruptFieldModel;

import static org.fusesource.jansi.Ansi.ansi;

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
                if (eruptFieldModel.getEruptField().edit().choiceType().length == 0) {
                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "字典类型未使用@DictType注解修饰");
                }
                break;
            case REFERENCE:
                if (eruptFieldModel.getEruptField().edit().referenceType().length == 0) {
                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "引用类型未使用@ReferenceType注解修饰");
                }
                if (eruptFieldModel.getEruptField().views().length > 0) {
                    for (View view : eruptFieldModel.getEruptField().views()) {
                        if ("".equals(view.column())) {
                            throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "引用类型@view注解未指定column值");
                        }
                    }
                }
                break;
            case BOOLEAN:
                if (!eruptFieldModel.getField().getType().getName().toLowerCase().equals("boolean")){
                    throw ExceptionUtil.styleEruptFieldException(eruptFieldModel, "使用@BoolType注解，要求返回值必须为boolean类型");
                }
                break;
        }
    }
}
