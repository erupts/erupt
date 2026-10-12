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

    /** Option value */
    private I id;

    /** Option text */
    private L label;

    /** Secondary text */
    private R remark;

    public CheckboxModel(I id, L label, R remark) {
        this.id = id;
        this.label = label;
        this.remark = remark;
    }

    public CheckboxModel() {
    }
}
