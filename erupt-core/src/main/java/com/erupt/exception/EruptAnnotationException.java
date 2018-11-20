package com.erupt.exception;

import com.erupt.model.EruptModel;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptAnnotationException extends RuntimeException {

    public EruptAnnotationException(String message) {
        super(message);
    }


    public static void validateEruptInfo(EruptModel eruptModel) {
        if (StringUtils.isBlank((eruptModel.getPrimaryKeyCol()))) {
            throw ExceptionUtil.styleEruptException(eruptModel, "无法识别主键，请在主键制字段中使用@EruptField注解");
        }
        if (null == eruptModel.getEruptFieldMap().get(eruptModel.getPrimaryKeyCol())) {
            throw ExceptionUtil.styleEruptException(eruptModel, "主键未找到erupt注解");
        }
    }
}
