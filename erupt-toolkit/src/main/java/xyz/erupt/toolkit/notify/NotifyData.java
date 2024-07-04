package xyz.erupt.toolkit.notify;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2024/6/29 19:04
 */
@Getter
@Setter
@NoArgsConstructor
public class NotifyData<T> {

    private DataAction action;

    private T data;

    public NotifyData(DataAction action, T data) {
        this.action = action;
        this.data = data;
    }
}
