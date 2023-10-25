package xyz.erupt.cloud.server.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.annotation.EruptCloudServer;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author YuePeng
 * date 2022/6/4 00:31
 */
@Slf4j
public class CloudServerUtil {

    public static EruptCloudServer.Proxy findEruptCloudServerAnnotation() {
        EruptCloudServer eruptCloudServer = EruptApplication.getPrimarySource().getAnnotation(EruptCloudServer.class);
        return null == eruptCloudServer ? null : EruptSpringUtil.getBean(eruptCloudServer.value());
    }

    //Node节点健康检查
    public static boolean nodeHealth(String nodeName, String location) {
        try {
            HttpResponse httpResponse = HttpUtil.createGet(location + CloudRestApiConst.NODE_HEALTH).timeout(1000).execute();
            String body = httpResponse.body();
            if (StringUtils.isNotBlank(body) && !nodeName.equals(body)) {
                log.warn("nodeName mismatch {} != {}", nodeName, body);
                return false;
            }
            return httpResponse.isOk();
        } catch (Exception e) {
            log.error(location, e);
            return false;
        }
    }

    //可重试的node节点检查
    @SneakyThrows
    public static boolean retryableNodeHealth(String nodeName, String location, int reqNum, int retryableGap) {
        if (reqNum <= 0) {
            log.error("remove node: {} -> {}", nodeName, location);
            return false;
        }
        if (nodeHealth(nodeName, location)) {
            return true;
        } else {
            Thread.sleep(retryableGap);
            return retryableNodeHealth(nodeName, location, reqNum - 1, retryableGap);
        }
    }

}
