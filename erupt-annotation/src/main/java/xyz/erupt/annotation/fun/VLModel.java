package xyz.erupt.annotation.fun;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

/**
 * @author YuePeng
 * date 2020-05-21
 */
@Getter
@Setter
public class VLModel {

    /** Option value */
    private String value;

    /** Option text */
    private String label;

    /** Secondary text */
    private String desc;

    /** Tag color */
    private String color;

    /** Whether the option cannot be selected */
    private boolean disable;

    /** Custom extension value */
    private Object extra;

    public VLModel(Long value, String label) {
        this.value = value + "";
        this.label = label;
    }

    public VLModel(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public VLModel(String value, String label, String desc) {
        this.value = value;
        this.label = label;
        this.desc = desc;
    }

    public VLModel(String value, String label, boolean disable) {
        this.value = value;
        this.label = label;
        this.disable = disable;
    }

    public VLModel(String value, String label, String desc, String color, boolean disable) {
        this.value = value;
        this.label = label;
        this.desc = desc;
        this.color = color;
        this.disable = disable;
    }

    public VLModel(VL vl) {
        this.value = vl.value();
        this.label = vl.label();
        this.desc = vl.desc();
        this.color = vl.color();
        this.disable = vl.disable();
        this.extra = vl.extra();
    }

    public VLModel() {
    }
}
