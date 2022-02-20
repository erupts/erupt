package xyz.erupt.upms.util;

import xyz.erupt.upms.enums.EruptFunPermissions;

/**
 * @author YuePeng
 * date 2022/1/15 00:21
 */
public class UPMSUtil {

    public static String getEruptFunPermissionsCode(String eruptName, EruptFunPermissions eruptFunPermissions) {
        return eruptName + "@" + eruptFunPermissions.name();
    }

}
