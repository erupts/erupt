package xyz.erupt.annotation.fun;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2020-05-21
 */
@Getter
@Setter
public class VLModel {

    private String value;

    private String label;

    private String desc;

    private boolean disable;

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
        this.desc = desc;
        this.disable = disable;
    }

    public VLModel(String value, String label, String desc, boolean disable) {
        this.value = value;
        this.label = label;
        this.desc = desc;
        this.disable = disable;
    }

    public VLModel() {
    }
}
