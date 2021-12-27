package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/26 23:57
 */
@Getter
@Setter
public class EruptContext {

    private String name;


    public EruptContext(String name) {
        this.name = name;
    }

    public EruptContext() {
    }
}
