package xyz.erupt.test.k8s_dashboard;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.k8s_dashboard.EruptK8sDashboardAutoConfiguration;
import xyz.erupt.k8s_dashboard.model.*;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The built-in dashboard models against fabric8's CRUD mock API server, pointed there through the
 * erupt.k8s-dashboard.master-url placeholder every model carries.
 */
class KubeDashboardTest extends EruptApplicationTests {

    private static final String MASTER_URL_PROPERTY = "erupt.k8s-dashboard.master-url";

    private static KubernetesMockServer server;

    private static KubernetesClient cluster;

    @Resource
    private EruptTokenService eruptTokenService;

    @Autowired
    private TestRestTemplate rest;

    @BeforeAll
    static void startCluster() {
        server = new KubernetesMockServer(new Context(), new MockWebServer(), new HashMap<>(), new KubernetesCrudDispatcher(), false);
        server.init();
        cluster = server.createClient();
        System.setProperty(MASTER_URL_PROPERTY, cluster.getConfiguration().getMasterUrl());
        cluster.namespaces().resource(new NamespaceBuilder().withNewMetadata().withName("demo").endMetadata().build()).create();
        cluster.apps().deployments().inNamespace("demo").resource(new DeploymentBuilder()
                .withNewMetadata().withName("web").withNamespace("demo").endMetadata()
                .withNewSpec().withReplicas(2).withNewTemplate().withNewSpec().addNewContainer().withName("web")
                .withImage("nginx:1.27").endContainer().endSpec().endTemplate().endSpec()
                .withNewStatus().withReadyReplicas(2).endStatus().build()).create();
        cluster.pods().inNamespace("demo").resource(new PodBuilder()
                .withNewMetadata().withName("web-1").withNamespace("demo").endMetadata()
                .withNewSpec().withNodeName("node-a").addNewContainer().withName("web").withImage("nginx:1.27").endContainer().endSpec()
                .withNewStatus().withPhase("Running").endStatus().build()).create();
    }

    @AfterAll
    static void stopCluster() {
        System.clearProperty(MASTER_URL_PROPERTY);
        cluster.close();
        server.destroy();
    }

    @BeforeEach
    void admin() {
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
    }

    @AfterEach
    void clear() {
        MetaContext.remove();
    }

    private static EruptModel model(Class<?> clazz) {
        return EruptCoreService.getErupt(clazz.getSimpleName());
    }

    private static Collection<Map<String, Object>> list(Class<?> clazz, Condition... conditions) {
        Page page = DataProcessorManager.getEruptDataProcessor(clazz).queryList(model(clazz), new Page(1, 100),
                EruptQuery.builder().conditions(new ArrayList<>(List.of(conditions))).build());
        return page.getList();
    }

    @Test
    void modelsReadTheConfiguredCluster() {
        Map<String, Object> pod = list(KubePod.class).iterator().next();
        assertEquals("demo/web-1", pod.get("qualifiedName"));
        assertEquals("Running", pod.get("phase"));
        assertEquals("node-a", pod.get("node"));
        assertEquals("nginx:1.27", pod.get("image"));

        Map<String, Object> web = list(KubeDeployment.class).iterator().next();
        assertEquals(2, web.get("replicas"));
        assertEquals(2, web.get("readyReplicas"));
        assertEquals("nginx:1.27", web.get("image"));

        assertTrue(list(KubeNamespace.class).stream().anyMatch(it -> "demo".equals(it.get("name"))));
    }

    @Test
    void eventsWaitForANamespace() {
        assertThrows(EruptApiErrorTip.class, () -> EruptUtil.checkEruptSearchNotnull(model(KubeEvent.class), List.of()));
        EruptUtil.checkEruptSearchNotnull(model(KubeEvent.class), List.of(new Condition("namespace", "demo", QueryExpression.EQ)));
    }

    @Test
    void terminalHasItsOwnButton() {
        List<MetaMenu> menus = new EruptK8sDashboardAutoConfiguration().initMenus();
        MetaMenu terminal = menus.stream().filter(it -> KubePod.TERMINAL.equals(it.getValue())).findFirst().orElseThrow();
        assertEquals(MenuTypeEnum.BUTTON.getCode(), terminal.getType());
        assertEquals(KubePod.class.getSimpleName(), terminal.getParentMenu().getValue());
    }

    @Test
    void terminalNeedsTheButton() {
        MetaMenu pods = MetaMenu.createEruptClassMenu(KubePod.class, null, 1);
        MetaMenu button = MetaMenu.createSimpleMenu(KubePod.TERMINAL, "Terminal", KubePod.TERMINAL, pods, 1, MenuTypeEnum.BUTTON.getCode());
        String url = "/erupt-api/tpl/operation-tpl/KubePod/exec?ids=demo/web-1&_token=";

        String viewer = this.login(-31L, List.of(pods));
        ResponseEntity<String> refused = rest.getForEntity(url + viewer, String.class);
        assertFalse(String.valueOf(refused.getBody()).contains("ticket:"), "pods alone do not open a shell");

        String operator = this.login(-32L, List.of(pods, button));
        ResponseEntity<String> granted = rest.getForEntity(url + operator, String.class);
        assertEquals(200, granted.getStatusCode().value());
        assertTrue(Objects.requireNonNull(granted.getBody()).contains("ticket:"), "the page carries a ticket per container");
        assertTrue(granted.getBody().contains("/erupt-k8s-dashboard/xterm/xterm.js"), "xterm is served from the jar, not a CDN");
        assertEquals(200, rest.getForEntity("/erupt-k8s-dashboard/xterm/xterm.js", String.class).getStatusCode().value());
    }

    private String login(long id, List<MetaMenu> menus) {
        MetaUserinfo user = new MetaUserinfo();
        user.setId(id);
        user.setAccount("k8s-user" + id);
        user.setUsername("k8s-user" + id);
        String token = Erupts.generateCode(22);
        eruptTokenService.loginToken(user, menus, token, 5);
        return token;
    }

}
