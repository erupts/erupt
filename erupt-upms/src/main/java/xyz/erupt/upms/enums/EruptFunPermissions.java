package xyz.erupt.upms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.core.constant.MenuTypeEnum;

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

    /**
     * Function permissions applicable to a menu type; null means the type carries no fun permissions.
     * FORM view only has add (first save) and edit (subsequent saves) semantics.
     */
    public static EruptFunPermissions[] byMenuType(String menuTypeCode) {
        if (MenuTypeEnum.TABLE.getCode().equals(menuTypeCode) || MenuTypeEnum.TREE.getCode().equals(menuTypeCode)) {
            return EruptFunPermissions.values();
        }
        if (MenuTypeEnum.FORM.getCode().equals(menuTypeCode)) {
            return new EruptFunPermissions[]{ADD, EDIT};
        }
        return null;
    }

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
