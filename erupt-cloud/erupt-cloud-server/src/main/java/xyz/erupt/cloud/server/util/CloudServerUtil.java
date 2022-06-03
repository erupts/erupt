package xyz.erupt.cloud.server.util;

import xyz.erupt.cloud.server.annotation.EruptCloudServer;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author YuePeng
 * date 2022/6/4 00:31
 */
public class CloudServerUtil {

    public static EruptCloudServer.Proxy findEruptCloudServerAnnotation() {
        EruptCloudServer eruptCloudServer = EruptApplication.getPrimarySource().getAnnotation(EruptCloudServer.class);
        return null == eruptCloudServer ? null : EruptSpringUtil.getBean(eruptCloudServer.value());
    }

}
