package xyz.erupt.core.util;

/**
 * @author YuePeng
 * date 2021/7/18 00:15
 */
public class EruptAssert {

    public static void notNull(Object[] arr, String message) {
        if (null == arr || arr.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

}
