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

    ADD("新增"),
    EDIT("修改"),
    DELETE("删除"),
    EXPORT("导出"),
    IMPORTABLE("导入"),
    VIEW_DETAIL("详情");

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
