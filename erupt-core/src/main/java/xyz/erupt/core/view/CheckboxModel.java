package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2018-11-15.
 */
@Getter
@Setter
public class CheckboxModel {

    private Object id;

    private Object label;

    public CheckboxModel(Object id, Object label) {
        this.id = id;
        this.label = label;
    }

    public CheckboxModel() {
    }
}
