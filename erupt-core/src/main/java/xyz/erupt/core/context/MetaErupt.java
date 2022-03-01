package xyz.erupt.core.context;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/27 20:57
 */
@Getter
@Setter
public class MetaErupt {

    private String name;

    public MetaErupt(String name) {
        this.name = name;
    }

    public MetaErupt() {
    }
}
