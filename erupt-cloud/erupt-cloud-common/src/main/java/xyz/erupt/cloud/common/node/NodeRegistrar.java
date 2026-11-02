package xyz.erupt.cloud.common.node;

import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.common.model.NodeInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The node's side of registration (PROTOCOL.md §3): registers with the erupt server and heart-beats, round-robin
 * over the server addresses, and deregisters on {@link #close}. JDK only, so the Spring node and the lite node
 * share it.
 *
 * @author YuePeng
 * date 2026/10/15
 */
public final class NodeRegistrar implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(NodeRegistrar.class.getName());

    // the form of REMOVE_INSTANCE_NODE
    private static final String PARAM_NODE_NAME = "nodeName", PARAM_ACCESS_TOKEN = "accessToken", PARAM_LOCATIONS = "locations";

    private final String nodeName, accessToken;
    private final List<String> servers;
    private final String instanceId = HexFormat.of().formatHex(new SecureRandom().generateSeed(3));
    private final AtomicInteger serverCycle = new AtomicInteger();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    // the addresses last registered, which close() deregisters
    private volatile String[] addresses;
    private Thread thread;
    private volatile boolean closed;

    /** {@code servers}: one or more erupt server base URLs. */
    public NodeRegistrar(String nodeName, String accessToken, Collection<String> servers) {
        if (servers.isEmpty()) throw new IllegalArgumentException("at least one server address is required");
        this.nodeName = nodeName;
        this.accessToken = accessToken;
        this.servers = servers.stream().map(s -> s.replaceAll("/+$", "")).toList();
    }

    /** Random per registrar, so per process start. */
    public String instanceId() {
        return instanceId;
    }

    public List<String> servers() {
        return servers;
    }

    /** The next server base URL, round-robin. */
    public String nextServer() {
        return servers.get(Math.floorMod(serverCycle.getAndIncrement(), servers.size()));
    }

    /**
     * The base URL a node listening on {@code port} under {@code path} (null for none) is reached on: this host's
     * interface toward the first server, which the server can route back to, unlike what the host name resolves to.
     */
    public String localAddress(String scheme, int port, String path) {
        String ip;
        try (DatagramSocket s = new DatagramSocket()) {
            URI uri = URI.create(servers.get(0));
            s.connect(new InetSocketAddress(uri.getHost() == null ? "127.0.0.1" : uri.getHost(), 80));
            ip = s.getLocalAddress().getHostAddress();
            if (ip.equals("0.0.0.0") || ip.equals("::") || ip.equals("0:0:0:0:0:0:0:0")) ip = "127.0.0.1";
        } catch (IOException | RuntimeException e) {
            ip = "127.0.0.1";
        }
        if (ip.contains(":")) ip = "[" + ip + "]";
        return scheme + "://" + ip + ":" + port + (path == null ? "" : path.replaceAll("/+$", ""));
    }

    /**
     * Registers and heart-beats every {@code heartbeat} (half that while failing) on a daemon thread until
     * {@link #close}; once only. {@code info} is asked anew for
     * every beat; its instance id, node name and access token are filled in here. Call it once the node's HTTP
     * server listens: the erupt server probes {@code /node-health} while registering.
     */
    public synchronized void start(Duration heartbeat, Supplier<NodeInfo> info) {
        if (thread != null || closed) return;
        thread = new Thread(() -> loop(heartbeat, info), "erupt-node-register");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized boolean started() {
        return thread != null;
    }

    private void loop(Duration heartbeat, Supplier<NodeInfo> info) {
        int failures = 0;
        while (!closed) {
            String server = nextServer();
            long wait = heartbeat.toMillis();
            try {
                NodeInfo nodeInfo = info.get();
                nodeInfo.setInstanceId(instanceId);
                nodeInfo.setNodeName(nodeName);
                nodeInfo.setAccessToken(accessToken);
                addresses = nodeInfo.getNodeAddress();
                HttpResponse<String> resp = http.send(HttpRequest.newBuilder(URI.create(server + CloudRestApiConst.REGISTER_NODE))
                        .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json(nodeInfo))).build(), HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() / 100 != 2) throw new IOException("HTTP " + resp.statusCode() + " " + resp.body());
                if (failures > 0) LOG.log(System.Logger.Level.INFO, server + " -> connection success");
                failures = 0;
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                wait /= 2;
                LOG.log(System.Logger.Level.ERROR, server + " -> register failed (retry " + ++failures + "): " + e.getMessage());
            }
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /** Stops heart-beating and deregisters the addresses last registered, so the server stops routing here at once. */
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        if (thread == null) return;
        thread.interrupt();
        String[] registered = addresses;
        String form = PARAM_NODE_NAME + "=" + encode(nodeName) + "&" + PARAM_ACCESS_TOKEN + "=" + encode(accessToken)
                + (registered == null || registered.length == 0 ? "" : "&" + PARAM_LOCATIONS + "=" + encode(String.join(",", registered)));
        try {
            HttpResponse<String> resp = http.send(HttpRequest.newBuilder(URI.create(nextServer() + CloudRestApiConst.REMOVE_INSTANCE_NODE))
                    .timeout(Duration.ofSeconds(5)).header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form)).build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) LOG.log(System.Logger.Level.ERROR, "deregister failed: HTTP " + resp.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.log(System.Logger.Level.ERROR, "deregister failed: " + e);
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- the register payload, without a JSON library

    // every NodeInfo field, named as declared, nulls left out
    static String json(NodeInfo info) {
        StringBuilder sb = new StringBuilder("{");
        for (Field field : NodeInfo.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            Object value;
            try {
                field.setAccessible(true);
                value = field.get(info);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            if (value instanceof Object[] array) value = List.of(array);
            field(sb, field.getName(), value);
        }
        return sb.append('}').toString();
    }

    private static void field(StringBuilder sb, String name, Object value) {
        if (value == null) return;
        if (sb.length() > 1) sb.append(',');
        value(sb.append('"').append(name).append("\":"), value);
    }

    private static void value(StringBuilder sb, Object value) {
        if (value instanceof Collection<?> c) {
            sb.append('[');
            int i = 0;
            for (Object it : c) value(i++ > 0 ? sb.append(',') : sb, it);
            sb.append(']');
        } else if (value instanceof Map<?, ?> m) {
            sb.append('{');
            int i = 0;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                value(i++ > 0 ? sb.append(',') : sb, String.valueOf(e.getKey()));
                value(sb.append(':'), e.getValue());
            }
            sb.append('}');
        } else if (value == null) {
            sb.append("null");
        } else {
            sb.append('"');
            for (char ch : value.toString().toCharArray()) {
                switch (ch) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                        else sb.append(ch);
                    }
                }
            }
            sb.append('"');
        }
    }

}
