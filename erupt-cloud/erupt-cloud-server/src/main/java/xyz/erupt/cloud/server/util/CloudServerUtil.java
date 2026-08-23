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

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

/**
 * @author YuePeng
 * date 2022/6/4 00:31
 */
@Slf4j
public class CloudServerUtil {

    /**
     * Whether a forward failure means the request never reached the node application (connection was
     * never established). Only such failures are safe to fail over to another instance — a read
     * timeout or an application error may have already been processed, and retrying could duplicate a
     * write.
     */
    public static boolean isConnectFailure(Throwable e) {
        while (null != e) {
            if (e instanceof ConnectException || e instanceof UnknownHostException || e instanceof NoRouteToHostException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    public static EruptCloudServer.Proxy findEruptCloudServerAnnotation() {
        EruptCloudServer eruptCloudServer = EruptApplication.getPrimarySource().getAnnotation(EruptCloudServer.class);
        return null == eruptCloudServer ? null : EruptSpringUtil.getBean(eruptCloudServer.value());
    }

    //Node health check
    public static boolean nodeHealth(String nodeName, String location) {
        try (HttpResponse httpResponse = HttpUtil.createGet(location + CloudRestApiConst.NODE_HEALTH).timeout(1000).execute()) {
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

    //Retryable node health check
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
