package xyz.erupt.atlas.constant;

import xyz.erupt.core.constant.EruptRestPath;

/**
 * @author YuePeng
 */
public class AtlasConstant {

    public static final String REST_ATLAS = EruptRestPath.ERUPT_API + "/erupt-atlas";

    public static final String MENU_ROOT = "atlas";

    // tpl page file name: doubles as the menu value and the @EruptMenuAuth permission key
    public static final String MENU_ATLAS = "erupt-atlas.html";

    // Console route of the graph page; append a {field} placeholder that the frontend fills from the row (OpenWay.ROUTER)
    public static final String ROUTE_ATLAS = "/tpl/" + MENU_ATLAS + "?erupt=";

}
