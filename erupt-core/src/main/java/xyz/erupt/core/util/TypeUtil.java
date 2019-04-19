package xyz.erupt.core.util;

import java.io.Serializable;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class TypeUtil {

    private static final String[] SIMPLE_JPA_TYPE = {
            "byte", "short", "int", "integer", "long", "float", "double", "boolean", "char", "string", "date"
    };

    /**
     * 将未知类型转换为目标类型（目标类型只支持基本数据类型）
     *
     * @param serializable
     * @param targetType
     * @return
     */
    public static Serializable typeStrConvertObject(Serializable serializable, String targetType) {
        targetType = targetType.toLowerCase();
        Serializable o;
        if ("float".equals(targetType)) {
            o = new Float(serializable.toString());
        } else if ("double".equals(targetType)) {
            o = new Double(serializable.toString());
        } else if ("long".equals(targetType)) {
            o = Long.valueOf(serializable.toString());
        } else if ("int".equals(targetType) || "integer".equals(targetType)) {
            o = Integer.valueOf(serializable.toString());
        } else {
            o = serializable.toString();
        }
        return o;
    }

    /**
     * 判断实体类字段返回值是否为基本类型（包括String与date）
     *
     * @return
     */
    public static boolean isFieldSimpleType(String typeName) {
        for (String simpleType : SIMPLE_JPA_TYPE) {
            if (simpleType.equals(typeName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
