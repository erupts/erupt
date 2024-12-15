package xyz.erupt.core.event;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2024/11/26 22:54
 */
@Getter
@Setter
public class EruptAddEvent<T> extends EruptEventBase<T> {

    public EruptAddEvent(Class<?> clazz, T source) {
        super(clazz, source);
    }
}
