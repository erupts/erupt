package xyz.erupt.core.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author YuePeng
 * date 2024/11/27 19:47
 */
@Getter
@Setter
public abstract class EruptEventBase<T> extends ApplicationEvent {

    private final Class<?> eruptClass;

    public EruptEventBase(Class<?> clazz, T source) {
        super(source);
        this.eruptClass = clazz;
    }

    @Override
    public T getSource() {
        return (T) source;
    }

}
