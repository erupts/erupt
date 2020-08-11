package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liyuepeng
 * @date 2020-05-21
 */
@Getter
@Setter
public class VL {

    private String value;

    private String label;

    public VL(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
