package com.erupt.exception;

import com.erupt.model.EruptFieldModel;
import com.erupt.model.EruptModel;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptAnnotationException extends RuntimeException {

    public EruptAnnotationException(String message) {
        super(message);
    }


    public static void validateEruptFieldInfo(EruptModel eruptModel) {

    }
}
