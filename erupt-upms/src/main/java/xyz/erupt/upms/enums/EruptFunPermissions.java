package xyz.erupt.upms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.annotation.sub_erupt.Power;

/**
 * @author YuePeng
 * date 2022/1/14 21:30
 */
@AllArgsConstructor
@Getter
public enum EruptFunPermissions {

    ADD("ADD"),
    EDIT("EDIT"),
    DELETE("DELETE"),
    EXPORT("EXPORT"),
    IMPORTABLE("IMPORT"),
    VIEW_DETAIL("DETAIL");

    private final String name;

    public boolean verifyPower(Power power) {
        if (power.add() && EruptFunPermissions.ADD == this) {
            return true;
        } else if (power.edit() && EruptFunPermissions.EDIT == this) {
            return true;
        } else if (power.delete() && EruptFunPermissions.DELETE == this) {
            return true;
        } else if (power.export() && EruptFunPermissions.EXPORT == this) {
            return true;
        } else if (power.importable() && EruptFunPermissions.IMPORTABLE == this) {
            return true;
        }
        return power.viewDetails() && EruptFunPermissions.VIEW_DETAIL == this;
    }

}
