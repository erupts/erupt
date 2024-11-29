package xyz.erupt.core.event;

import lombok.Getter;

/**
 * @author YuePeng
 * date 2024/11/26 22:54
 */
@Getter
public class EruptEditEvent<T> extends EruptEventBase<T> {

    public final T before;

    public EruptEditEvent(Class<?> clazz, T source, T before) {
        super(clazz, source);
        this.before = before;
    }

}
