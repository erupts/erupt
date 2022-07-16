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

    private String menuValue; //当前菜单值

    public MetaErupt(String name) {
        this.name = name;
    }

    public MetaErupt(String name, String menuValue) {
        this.name = name;
        this.menuValue = menuValue;
    }

    public MetaErupt() {
    }
}
