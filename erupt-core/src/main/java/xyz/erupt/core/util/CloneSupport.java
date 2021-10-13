package xyz.erupt.core.util;

/**
 * @author YuePeng
 * date 2021/10/13 21:35
 */
public class CloneSupport<T> implements Cloneable {

    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
