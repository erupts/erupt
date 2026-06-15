package xyz.erupt.monitor.constant;

import xyz.erupt.core.constant.EruptRestPath;

/**
 * @author YuePeng
 * date 2021/1/29 18:25
 */
public class MonitorConstant {

    public static final String REST_MONITOR = EruptRestPath.ERUPT_API + "/erupt-monitor";

    // router menu values: double as the Angular route and the @EruptMenuAuth permission key
    public static final String MENU_SERVER = "/monitor/server";

    public static final String MENU_REDIS = "/monitor/redis";

    public static final String MENU_DIAGNOSIS = "/monitor/diagnosis";

}
