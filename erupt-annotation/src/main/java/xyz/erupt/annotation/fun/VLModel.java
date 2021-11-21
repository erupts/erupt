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

    // 额外描述
    private String desc;

    // 是否禁用
    private boolean disable;

//    private String color; //标签颜色

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
