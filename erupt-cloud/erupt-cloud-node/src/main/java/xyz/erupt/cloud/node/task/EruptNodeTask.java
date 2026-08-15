package xyz.erupt.cloud.node.task;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
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
import xyz.erupt.cloud.node.config.EruptNodeInterceptor;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.view.EruptModel;

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

    private int retryCount = 0;

    // This instance's registered addresses, captured so shutdown can deregister them precisely.
    private volatile String[] nodeAddresses;

    @Override
    @SuppressWarnings("StringConcatenationArgumentToLogCall")
    public void run(ApplicationArguments args) {
        log.info(ansi().fgBright(Ansi.Color.CYAN) + " \n" +
                "                 _                _     \n" +
                " ___ ___ _ _ ___| |_    ___ ___ _| |___ \n" +
                "| -_|  _| | | . |  _|  |   | . | . | -_|\n" +
                "|___|_| |___|  _|_|    |_|_|___|___|___|\n" +
                "            |_|        " + ansi().fgBright(Ansi.Color.BLACK).a(EruptInformation.getEruptVersion()).reset() + "\n" +
                ansi().reset()
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
        if (null == eruptNodeProp.getServerAddresses() || eruptNodeProp.getServerAddresses().length == 0) {
            throw new RuntimeException(EruptNodeProp.SPACE + ".serverAddresses not config");
        }
        if (null == eruptNodeProp.getNodeName()) {
            throw new RuntimeException(EruptNodeProp.SPACE + ".nodeName not config");
        }
        String sep = ansi().fgBright(Ansi.Color.BLACK).a("─".repeat(54)).reset().toString();
        String address = eruptNodeProp.getBalanceAddress();
        log.info(sep);
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Node     ").reset(), ansi().fgBright(Ansi.Color.CYAN).a(eruptNodeProp.getNodeName()).reset());
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Server   ").reset(), address);
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Version  ").reset(), EruptInformation.getEruptVersion());
        log.info(sep);
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
            this.nodeAddresses = nodeInfo.getNodeAddress();
            try {
                try (HttpResponse httpResponse = HttpUtil.createPost(address + CloudRestApiConst.REGISTER_NODE)
                        .body(gson.toJson(nodeInfo)).execute()) {
                    if (!httpResponse.isOk()) {
                        log.error("{} -> Http error: {}", address, httpResponse.body());
                    }
                }
                if (this.errorConnect) {
                    this.errorConnect = false;
                    this.retryCount = 0;
                    log.info("{} -> {}", address, ansi().fgBright(Ansi.Color.GREEN).a("Connection success").reset());
                }
                TimeUnit.MILLISECONDS.sleep(eruptNodeProp.getHeartbeatTime());
            } catch (Exception e) {
                this.retryCount++;
                log.error("{} -> Connection error (retry {}): {}", address, this.retryCount, e.getMessage());
                this.errorConnect = true;
                TimeUnit.MILLISECONDS.sleep(eruptNodeProp.getHeartbeatTime() / 2);
            }
        }
    }

    @Override
    public void destroy() {
        this.runner = false;
        // 1. Deregister this instance's addresses so the server stops routing new requests to it
        //    immediately, instead of waiting for the survival check to notice the node is gone.
        try (HttpResponse httpResponse = HttpUtil.createPost(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.REMOVE_INSTANCE_NODE)
                .form(new HashMap<String, Object>() {{
                    put("nodeName", eruptNodeProp.getNodeName());
                    put("accessToken", eruptNodeProp.getAccessToken());
                    if (null != nodeAddresses && nodeAddresses.length > 0) {
                        put("locations", String.join(",", nodeAddresses));
                    }
                }}).execute()) {
            if (!httpResponse.isOk()) {
                log.error("deregister failed: {}", httpResponse.body());
            }
        } catch (Exception e) {
            log.error("deregister error: {}", e.getMessage());
        }
        // 2. Drain: wait for requests already accepted before this instance was deregistered to finish.
        long deadline = System.currentTimeMillis() + eruptNodeProp.getDrainTimeout();
        while (EruptNodeInterceptor.inFlight() > 0 && System.currentTimeMillis() < deadline) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        int remaining = EruptNodeInterceptor.inFlight();
        if (remaining > 0) {
            log.warn("shutdown drain timeout, {} request(s) still in flight", remaining);
        }
    }
}
