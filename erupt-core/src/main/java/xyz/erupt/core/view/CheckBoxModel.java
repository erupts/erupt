package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liyuepeng
 * @date 2018-11-15.
 */
@Getter
@Setter
public class CheckBoxModel {

    private String id;

    private String label;

    public CheckBoxModel(Object id, Object label) {
        if (null != id) {
            this.id = id.toString();
        }
        if (null != label) {
            this.label = label.toString();
        }
    }

    public CheckBoxModel() {
    }
}
