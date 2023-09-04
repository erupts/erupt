package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2018-11-15.
 */
@Getter
@Setter
public class CheckboxModel<I, L, R> {

    private I id;

    private L label;

    private R remark;

    public CheckboxModel(I id, L label, R remark) {
        this.id = id;
        this.label = label;
        this.remark = remark;
    }

    public CheckboxModel() {
    }
}
