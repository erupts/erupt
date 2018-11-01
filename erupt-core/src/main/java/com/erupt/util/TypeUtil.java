package com.erupt.util;

import java.io.Serializable;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class TypeUtil {


    /**
     * 将未知类型转换为目标类型（目标类型只支持基本数据类型）
     *
     * @param serializable
     * @param targetType
     * @return
     */
    public static Object typeStrConvertObject(Serializable serializable, String targetType) {
        targetType = targetType.toLowerCase();
        Object o = null;
        if ("float".equals(targetType)) {
            o = new Float(serializable.toString());
        } else if ("double".equals(targetType)) {
            o = new Double(serializable.toString());
        } else if ("long".equals(targetType)) {
            o = new Long(serializable.toString());
        } else if ("int".equals(targetType)) {
            o = new Long(serializable.toString());
        } else {
            o = serializable.toString();
        }
        return o;
    }
}
