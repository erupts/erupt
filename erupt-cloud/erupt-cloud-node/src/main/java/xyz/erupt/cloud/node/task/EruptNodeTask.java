package xyz.erupt.cloud.node.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.common.model.NodeInfo;
import xyz.erupt.cloud.common.node.NodeRegistrar;
import xyz.erupt.cloud.node.config.EruptNodeInterceptor;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.cloud.node.handler.NodeInfoHandler;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptInformation;
import xyz.erupt.core.view.EruptModel;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Registers the node with the erupt server through {@link NodeRegistrar} once the web server listens, and on
 * shutdown deregisters it and drains the requests in flight.
 *
 * @author YuePeng
 * date 2021/12/17 00:24
 */
@Component
@Slf4j
@Order
public class EruptNodeTask implements ApplicationRunner, DisposableBean {

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Resource
    private ServerProperties serverProperties;

    @Resource
    private ObjectProvider<NodeInfoHandler> nodeInfoHandlers;

    // the port the application's web server actually listens on (server.port may be unset or 0)
    private volatile Integer port;

    private NodeRegistrar registrar;

    @EventListener
    public void webServerInitialized(WebServerInitializedEvent event) {
        // the management server, on a port of its own, has a namespace
        if (null == event.getApplicationContext().getServerNamespace()) this.port = event.getWebServer().getPort();
    }

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
        if (!eruptNodeProp.isEnableRegister()) {
            log.warn("erupt-node registration disabled");
            return;
        }
        if (null == eruptNodeProp.getServerAddresses() || eruptNodeProp.getServerAddresses().length == 0) {
            throw new IllegalStateException(EruptNodeProp.SPACE + ".serverAddresses not config");
        }
        if (null == eruptNodeProp.getNodeName()) {
            throw new IllegalStateException(EruptNodeProp.SPACE + ".nodeName not config");
        }
        registrar = new NodeRegistrar(eruptNodeProp.getNodeName(), eruptNodeProp.getAccessToken(), List.of(eruptNodeProp.getServerAddresses()));
        String[] nodeAddress = null != eruptNodeProp.getHostAddress() && eruptNodeProp.getHostAddress().length > 0
                ? eruptNodeProp.getHostAddress()
                : new String[]{registrar.localAddress(eruptNodeProp.getSchema(),
                Optional.ofNullable(port).or(() -> Optional.ofNullable(serverProperties.getPort())).orElse(8080),
                serverProperties.getServlet().getContextPath())};
        String sep = ansi().fgBright(Ansi.Color.BLACK).a("─".repeat(54)).reset().toString();
        log.info(sep);
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Node     ").reset(), ansi().fgBright(Ansi.Color.CYAN).a(eruptNodeProp.getNodeName()).reset());
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Address  ").reset(), String.join(", ", nodeAddress));
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Server   ").reset(), String.join(", ", registrar.servers()));
        log.info("  {}{}", ansi().fgBright(Ansi.Color.BLACK).a("Version  ").reset(), EruptInformation.getEruptVersion());
        log.info(sep);
        registrar.start(Duration.ofMillis(eruptNodeProp.getHeartbeatTime()), () -> {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setVersion(EruptInformation.getEruptVersion());
            nodeInfo.setEruptModules(EruptCoreService.getModules());
            nodeInfo.setNodeAddress(nodeAddress);
            nodeInfo.setErupts(EruptCoreService.getErupts().stream().map(EruptModel::getEruptName).toList());
            nodeInfoHandlers.forEach(handler -> handler.handle(nodeInfo));
            return nodeInfo;
        });
    }

    @Override
    public void destroy() {
        // 1. Deregister this instance's addresses so the server stops routing new requests to it
        //    immediately, instead of waiting for the survival check to notice the node is gone.
        if (null != registrar) registrar.close();
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
