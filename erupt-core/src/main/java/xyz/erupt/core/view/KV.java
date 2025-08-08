package xyz.erupt.core.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/8/8 22:27
 */
@Getter
@Setter
@NoArgsConstructor
public class KV<KEY, VALUE> {

    private KEY key;

    private VALUE value;

    public KV(KEY key, VALUE value) {
        this.key = key;
        this.value = value;
    }

}
