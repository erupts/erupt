package xyz.erupt.test.k8s;

import com.google.gson.JsonObject;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.k8s.operation.K8sRestartHandler;
import xyz.erupt.k8s.operation.K8sScaleForm;
import xyz.erupt.k8s.operation.K8sScaleHandler;
import xyz.erupt.k8s.operation.K8sYamlHandler;
import xyz.erupt.k8s_dashboard.terminal.K8sTerminalTicketService;
import xyz.erupt.k8s_dashboard.terminal.K8sTerminalTplHandler;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.k8s.model.K8sDeploymentModel;
import xyz.erupt.test.k8s.model.K8sPodModel;

import org.springframework.boot.test.web.server.LocalServerPort;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.upms.service.EruptTokenService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * erupt-data-k8s against fabric8's CRUD mock API server: list push-down, namespace scoping,
 * YAML writes and the workload row operations.
 */
class EruptK8sDataTest extends EruptApplicationTests {

    private static final String MASTER_URL_PROPERTY = "erupt-test.k8s.master-url";

    private static KubernetesMockServer server;

    private static KubernetesClient cluster;

    @Resource
    private EruptModifyService eruptModifyService;

    @Resource
    private K8sScaleHandler scaleHandler;

    @Resource
    private K8sRestartHandler restartHandler;

    @Resource
    private K8sYamlHandler yamlHandler;

    @Resource
    private K8sTerminalTplHandler terminalTplHandler;

    @Resource
    private K8sTerminalTicketService ticketService;

    @Resource
    private EruptTokenService eruptTokenService;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void startCluster() {
        server = new KubernetesMockServer(new Context(), new MockWebServer(), new HashMap<>(), new KubernetesCrudDispatcher(), false);
        server.init();
        cluster = server.createClient();
        // Read live by the model's ${...} placeholder, so the shared Spring context needs no restart
        System.setProperty(MASTER_URL_PROPERTY, cluster.getConfiguration().getMasterUrl());
    }

    @AfterAll
    static void stopCluster() {
        System.clearProperty(MASTER_URL_PROPERTY);
        cluster.close();
        server.destroy();
    }

    @BeforeEach
    void seed() {
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
        cluster.apps().deployments().inNamespace("demo").delete();
        cluster.pods().inAnyNamespace().delete();
        cluster.apps().deployments().inNamespace("demo").resource(this.deployment("web", "web", 2)).create();
        cluster.apps().deployments().inNamespace("demo").resource(this.deployment("api", "api", 1)).create();
        cluster.pods().inNamespace("demo").resource(this.pod("p1", "demo")).create();
        cluster.pods().inNamespace("other").resource(this.pod("p2", "other")).create();
    }

    @AfterEach
    void clear() {
        MetaContext.remove();
    }

    private Deployment deployment(String name, String app, int replicas) {
        return new DeploymentBuilder().withNewMetadata().withName(name).withNamespace("demo").addToLabels("app", app).endMetadata()
                .withNewSpec().withReplicas(replicas).withNewSelector().addToMatchLabels("app", app).endSelector()
                .withNewTemplate().withNewMetadata().addToLabels("app", app).endMetadata()
                .withNewSpec().addNewContainer().withName(app).withImage("nginx").endContainer().endSpec()
                .endTemplate().endSpec().build();
    }

    private Pod pod(String name, String namespace) {
        return new PodBuilder().withNewMetadata().withName(name).withNamespace(namespace).endMetadata()
                .withNewSpec().addNewContainer().withName("main").withImage("busybox").endContainer().endSpec()
                .withNewStatus().withPhase("Running").endStatus().build();
    }

    private static EruptModel model(Class<?> clazz) {
        return EruptCoreService.getErupt(clazz.getSimpleName());
    }

    private static IEruptDataService data(Class<?> clazz) {
        return DataProcessorManager.getEruptDataProcessor(clazz);
    }

    private Collection<Map<String, Object>> list(Class<?> clazz, Condition... conditions) {
        Page page = data(clazz).queryList(model(clazz), new Page(1, 100),
                EruptQuery.builder().conditions(new ArrayList<>(List.of(conditions))).build());
        return page.getList();
    }

    private String manifest(String name, String namespace, int replicas) {
        return "apiVersion: apps/v1\nkind: Deployment\nmetadata:\n  name: " + name + "\n"
                + (null == namespace ? "" : "  namespace: " + namespace + "\n")
                + "  labels:\n    app: " + name + "\nspec:\n  replicas: " + replicas + "\n"
                + "  selector:\n    matchLabels:\n      app: " + name + "\n"
                + "  template:\n    metadata:\n      labels:\n        app: " + name + "\n"
                + "    spec:\n      containers:\n      - name: main\n        image: nginx\n";
    }

    private JsonObject yamlBody(String yaml) {
        JsonObject body = new JsonObject();
        body.addProperty("yaml", yaml);
        return body;
    }

    @Test
    void labelSelectorRunsOnTheServer() {
        assertEquals(2, this.list(K8sDeploymentModel.class).size());
        Collection<Map<String, Object>> web = this.list(K8sDeploymentModel.class, new Condition("labelSelector", "app=web", QueryExpression.EQ));
        assertEquals(1, web.size());
        assertEquals(2, web.iterator().next().get("replicas"));
    }

    @Test
    void allNamespacesModelNarrowsByNamespace() {
        assertEquals(2, this.list(K8sPodModel.class).size());
        Collection<Map<String, Object>> other = this.list(K8sPodModel.class, new Condition("namespace", "other", QueryExpression.EQ));
        assertEquals(1, other.size());
        assertEquals("other/p2", other.iterator().next().get("qualifiedName"));
        K8sPodModel pod = (K8sPodModel) data(K8sPodModel.class).findDataById(model(K8sPodModel.class), "other/p2");
        assertEquals("Running", pod.getPhase());
    }

    @Test
    void fixedNamespaceNeverWidens() {
        assertThrows(EruptWebApiRuntimeException.class,
                () -> data(K8sDeploymentModel.class).findDataById(model(K8sDeploymentModel.class), "other/web"));
    }

    @Test
    void detailCarriesTheManifest() {
        K8sDeploymentModel web = (K8sDeploymentModel) data(K8sDeploymentModel.class).findDataById(model(K8sDeploymentModel.class), "web");
        assertTrue(web.getYaml().contains("kind: Deployment"), web.getYaml());
        assertFalse(web.getYaml().contains("managedFields"));
        assertNull(this.list(K8sDeploymentModel.class).iterator().next().get("yaml"), "lists skip the manifest");
    }

    @Test
    void createFromYaml() {
        Object id = eruptModifyService.insertEruptData(model(K8sDeploymentModel.class), this.yamlBody(this.manifest("cache", null, 3)));
        assertEquals("cache", id, "the server-assigned identity comes back");
        Deployment cache = cluster.apps().deployments().inNamespace("demo").withName("cache").get();
        assertNotNull(cache, "a manifest without a namespace lands in the model's namespace");
        assertEquals(3, cache.getSpec().getReplicas());
    }

    @Test
    void createRefusesOtherKindsAndNamespaces() {
        String pod = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: sneaky\nspec:\n  containers:\n  - name: c\n    image: busybox\n";
        assertThrows(EruptWebApiRuntimeException.class,
                () -> eruptModifyService.insertEruptData(model(K8sDeploymentModel.class), this.yamlBody(pod)));
        assertThrows(EruptWebApiRuntimeException.class,
                () -> eruptModifyService.insertEruptData(model(K8sDeploymentModel.class), this.yamlBody(this.manifest("x", "other", 1))));
        assertNull(cluster.pods().inNamespace("demo").withName("sneaky").get());
        assertNull(cluster.apps().deployments().inNamespace("other").withName("x").get());
    }

    @Test
    void editFromYaml() {
        K8sDeploymentModel web = (K8sDeploymentModel) data(K8sDeploymentModel.class).findDataById(model(K8sDeploymentModel.class), "web");
        JsonObject body = this.yamlBody(web.getYaml().replace("replicas: 2", "replicas: 5"));
        body.addProperty("name", "web");
        eruptModifyService.updateEruptData(model(K8sDeploymentModel.class), body);
        assertEquals(5, cluster.apps().deployments().inNamespace("demo").withName("web").get().getSpec().getReplicas());

        JsonObject renamed = this.yamlBody(this.manifest("other-name", null, 1));
        renamed.addProperty("name", "web");
        assertThrows(EruptWebApiRuntimeException.class,
                () -> eruptModifyService.updateEruptData(model(K8sDeploymentModel.class), renamed));
    }

    @Test
    void deleteById() {
        eruptModifyService.deleteEruptData(model(K8sDeploymentModel.class), List.of("api"), false);
        assertNull(cluster.apps().deployments().inNamespace("demo").withName("api").get());
    }

    @Test
    void scaleRestartAndYamlOperations() {
        Object web = data(K8sDeploymentModel.class).findDataById(model(K8sDeploymentModel.class), "web");
        assertEquals(2, scaleHandler.eruptFormValue(List.of(web), new K8sScaleForm(), new String[0]).getReplicas());
        K8sScaleForm form = new K8sScaleForm();
        form.setReplicas(4);
        scaleHandler.exec(List.of(web), form, new String[0]);
        assertEquals(4, cluster.apps().deployments().inNamespace("demo").withName("web").get().getSpec().getReplicas());

        restartHandler.exec(List.of(web), null, new String[0]);
        Map<String, String> annotations = cluster.apps().deployments().inNamespace("demo").withName("web").get()
                .getSpec().getTemplate().getMetadata().getAnnotations();
        assertNotNull(annotations.get("kubectl.kubernetes.io/restartedAt"));

        assertTrue(yamlHandler.exec(List.of(web), null, new String[0]).startsWith("codeDrawer('yaml'"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void terminalTicketsAreBoundAndSingleUse() {
        MetaContext.registerToken("terminal-test-token");
        Object pod = data(K8sPodModel.class).findDataById(model(K8sPodModel.class), "other/p2");
        Map<String, Object> binding = new HashMap<>();
        binding.put("rows", List.of(pod));
        terminalTplHandler.bindTplData(binding, new String[]{"logs"});
        assertEquals("logs", binding.get("mode"));
        List<Map<String, String>> containers = (List<Map<String, String>>) binding.get("containers");
        assertEquals("main", containers.get(0).get("name"));

        K8sTerminalTicketService.Ticket ticket = ticketService.consume(containers.get(0).get("ticket"));
        assertNotNull(ticket);
        assertEquals("other", ticket.namespace(), "the pod comes from the server-checked row, not the browser");
        assertEquals("p2", ticket.pod());
        assertEquals("terminal-test-token", ticket.token());
        assertEquals(K8sTerminalTicketService.Mode.LOGS, ticket.mode());
        assertNull(ticketService.consume(containers.get(0).get("ticket")), "a ticket opens one socket");
    }

    // Opens the terminal socket and returns the close code plus any text the server sent first
    private String[] openTerminal(String ticket) throws Exception {
        CompletableFuture<String[]> result = new CompletableFuture<>();
        StringBuilder received = new StringBuilder();
        HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:" + port + "/erupt-k8s-terminal?ticket=" + ticket), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                        received.append(StandardCharsets.UTF_8.decode(data));
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        result.complete(new String[]{String.valueOf(statusCode), received.toString()});
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        result.complete(new String[]{"error", error.getMessage()});
                    }
                }).get(5, TimeUnit.SECONDS);
        return result.get(10, TimeUnit.SECONDS);
    }

    @Test
    void terminalSocketRefusesBadTickets() throws Exception {
        assertEquals("1008", this.openTerminal("not-a-ticket")[0]);

        // A real ticket whose session has ended is refused too
        String stale = ticketService.issue("logged-out-token", K8sPodModel.class, "other", "p2", "main", K8sTerminalTicketService.Mode.LOGS);
        assertEquals("1008", this.openTerminal(stale)[0]);
    }

    @Test
    void terminalSocketReachesThePod() throws Exception {
        MetaUserinfo user = new MetaUserinfo();
        user.setId(-7L);
        user.setAccount("k8s-terminal");
        user.setUsername("k8s-terminal");
        String token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, List.of(), token, 5);
        String ticket = ticketService.issue(token, K8sPodModel.class, "other", "p2", "main", K8sTerminalTicketService.Mode.EXEC);
        // The CRUD mock server has no exec support, so the bridge opens, reports the failure and closes
        String[] outcome = this.openTerminal(ticket);
        assertEquals("1011", outcome[0], Arrays.toString(outcome));
        assertFalse(outcome[1].isBlank(), "the failure is shown in the terminal");
    }

}
