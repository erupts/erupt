package xyz.erupt.cloud.node.task;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.common.model.NodeInfo;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.view.EruptModel;

import jakarta.annotation.Resource;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * @author YuePeng
 * date 2021/12/17 00:24
 */
@Component
@Slf4j
@Order
public class EruptNodeTask implements Runnable, ApplicationRunner, DisposableBean {

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Resource
    private ServerProperties serverProperties;

    private boolean runner = true;

    private final Gson gson = GsonFactory.getGson();

    private final String instanceId = RandomStringUtils.randomAlphabetic(6);

    private boolean errorConnect = false;

    private String hostName;

//    public EruptNodeTask() {
//        try {
//            hostName = InetAddress.getLocalHost().getHostName();
//        } catch (UnknownHostException ignore) {
//        }
//    }

    @Override
    public void run(ApplicationArguments args) {
        log.info(ansi().fg(Ansi.Color.BLUE) + " \n" +
                "                 _                _     \n" +
                " ___ ___ _ _ ___| |_    ___ ___ _| |___ \n" +
                "| -_|  _| | | . |  _|  |   | . | . | -_|\n" +
                "|___|_| |___|  _|_|    |_|_|___|___|___|\n" +
                "            |_|        " + EruptInformation.getEruptVersion() + "\n" +
                ansi().fg(Ansi.Color.DEFAULT)
        );
        if (eruptNodeProp.isEnableRegister()) {
            Thread register = new Thread(this);
            register.setName("erupt-node-register");
            register.setDaemon(true);
            register.start();
        } else {
            log.warn("erupt-node registration disabled");
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        if (null == eruptNodeProp.getServerAddresses() || eruptNodeProp.getServerAddresses().length <= 0) {
            throw new RuntimeException(EruptNodeProp.SPACE + ".serverAddresses not config");
        }
        if (null == eruptNodeProp.getNodeName()) {
            throw new RuntimeException(EruptNodeProp.SPACE + ".nodeName not config");
        }
        log.info("erupt-cloud-node initialization completed");
        while (this.runner) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setInstanceId(instanceId);
            nodeInfo.setNodeName(eruptNodeProp.getNodeName());
            nodeInfo.setAccessToken(eruptNodeProp.getAccessToken());
            nodeInfo.setVersion(EruptInformation.getEruptVersion());
            nodeInfo.setEruptModules(EruptCoreService.getModules());
            if (null != eruptNodeProp.getHostAddress() && eruptNodeProp.getHostAddress().length > 0) {
                nodeInfo.setNodeAddress(eruptNodeProp.getHostAddress());
            } else {
                String contextPath = serverProperties.getServlet().getContextPath() == null ? "" : serverProperties.getServlet().getContextPath();
                nodeInfo.setNodeAddress(new String[]{eruptNodeProp.getSchema() + "://" + Inet4Address.getLocalHost().getHostAddress() + ":" + serverProperties.getPort() + contextPath});
            }
            nodeInfo.setErupts(EruptCoreService.getErupts().stream().map(EruptModel::getEruptName).collect(Collectors.toList()));
            String address = eruptNodeProp.getBalanceAddress();
            try {
                HttpResponse httpResponse = HttpUtil.createPost(address + CloudRestApiConst.REGISTER_NODE)
                        .body(gson.toJson(nodeInfo)).execute();
                if (!httpResponse.isOk()) {
                    log.error(address + " -> Connection error: {}", httpResponse.body());
                }
                if (this.errorConnect) {
                    this.errorConnect = false;
                    log.info(address + " -> Connection success");
                }
                TimeUnit.MILLISECONDS.sleep(eruptNodeProp.getHeartbeatTime());
            } catch (Exception e) {
                log.error(address + " -> Connection error: {}", e.getMessage());
                this.errorConnect = true;
                TimeUnit.MILLISECONDS.sleep(eruptNodeProp.getHeartbeatTime() / 2);
            }
        }
    }

    @Override
    public void destroy() {
        this.runner = false;
        // cancel register
        HttpUtil.createPost(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.REMOVE_INSTANCE_NODE
        ).form(new HashMap<String, Object>() {{
            this.put("nodeName", eruptNodeProp.getNodeName());
            this.put("accessToken", eruptNodeProp.getAccessToken());
        }}).execute();
    }
}
