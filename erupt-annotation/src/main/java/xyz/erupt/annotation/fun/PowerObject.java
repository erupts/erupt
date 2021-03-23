package xyz.erupt.annotation.fun;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.sub_erupt.Power;

/**
 * @author YuePeng
 * date 2020-08-07
 */
@Getter
@Setter
public class PowerObject {

    private boolean add;

    private boolean delete;

    private boolean edit;

    private boolean query;

    private boolean viewDetails;

    private boolean export;

    private boolean importable;

    public PowerObject(Power power) {
        this.add = power.add();
        this.delete = power.delete();
        this.edit = power.edit();
        this.query = power.query();
        this.viewDetails = power.viewDetails();
        this.export = power.export();
        this.importable = power.importable();
    }
}
