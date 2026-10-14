package xyz.erupt.test.k8s;

import io.fabric8.kubernetes.api.model.ListMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.fabric8.mockwebserver.MockWebServer;
import io.fabric8.mockwebserver.http.Dispatcher;
import io.fabric8.mockwebserver.http.MockResponse;
import io.fabric8.mockwebserver.http.RecordedRequest;
import org.junit.jupiter.api.*;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.k8s.model.K8sPodModel;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Server-side paging against an API server that honours limit / continue the way a real one does,
 * which fabric8's CRUD mock does not.
 */
class K8sPagingTest extends EruptApplicationTests {

    private static final String MASTER_URL_PROPERTY = "erupt-test.k8s.master-url";

    private static final KubernetesSerialization JSON = new KubernetesSerialization();

    private static final List<Pod> PODS = new ArrayList<>();

    // every list and get the dashboard sent
    private static final List<String> REQUESTS = new CopyOnWriteArrayList<>();

    private static MockWebServer server;

    @BeforeAll
    static void start() {
        for (int i = 1; i <= 7; i++) {
            PODS.add(new PodBuilder().withNewMetadata().withName("p" + i).withNamespace("paged").endMetadata()
                    .withNewStatus().withPhase("Running").endStatus().build());
        }
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                REQUESTS.add(request.getPath());
                return K8sPagingTest.answer(request.getPath());
            }
        });
        server.start();
        System.setProperty(MASTER_URL_PROPERTY, server.url("/").toString());
    }

    @AfterAll
    static void stop() {
        System.clearProperty(MASTER_URL_PROPERTY);
        server.shutdown();
    }

    @BeforeEach
    void user() {
        REQUESTS.clear();
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
    }

    @AfterEach
    void clear() {
        MetaContext.remove();
    }

    // A list pages by an offset carried in the continue token; a get returns one pod
    private static MockResponse answer(String path) {
        String[] parts = path.split("\\?", 2);
        Map<String, String> query = new HashMap<>();
        if (parts.length > 1) {
            for (String pair : parts[1].split("&")) {
                String[] kv = pair.split("=", 2);
                query.put(kv[0], kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
            }
        }
        if (parts[0].equals("/api/v1/pods")) {
            int limit = Integer.parseInt(query.getOrDefault("limit", "500"));
            int offset = query.containsKey("continue") ? Integer.parseInt(query.get("continue")) : 0;
            int end = Math.min(offset + limit, PODS.size());
            ListMetaBuilder meta = new ListMetaBuilder();
            if (end < PODS.size()) meta.withContinue(String.valueOf(end)).withRemainingItemCount((long) PODS.size() - end);
            String body = JSON.asJson(new PodListBuilder().withMetadata(meta.build()).withItems(PODS.subList(offset, end)).build());
            return new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(body);
        }
        for (Pod pod : PODS) {
            if (parts[0].equals("/api/v1/namespaces/paged/pods/" + pod.getMetadata().getName())) {
                return new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(JSON.asJson(pod));
            }
        }
        return new MockResponse().setResponseCode(404).setBody("{\"kind\":\"Status\",\"code\":404}");
    }

    private static EruptModel model() {
        return EruptCoreService.getErupt(K8sPodModel.class.getSimpleName());
    }

    private static Page page(int index, Condition... conditions) {
        return DataProcessorManager.getEruptDataProcessor(K8sPodModel.class).queryList(model(), new Page(index, 3),
                EruptQuery.builder().conditions(new ArrayList<>(List.of(conditions))).build());
    }

    private static List<Object> names(Page page) {
        return page.getList().stream().map(it -> it.get("qualifiedName")).toList();
    }

    @Test
    void pagesAreReadFromTheServer() {
        Page first = page(1);
        assertEquals(List.of("paged/p1", "paged/p2", "paged/p3"), names(first));
        assertEquals(7, first.getTotal(), "the server's remaining count makes the total");
        assertEquals(1, REQUESTS.size(), "one page, one request");
        assertTrue(REQUESTS.get(0).contains("limit=3"), REQUESTS.get(0));

        REQUESTS.clear();
        Page second = page(2);
        assertEquals(List.of("paged/p4", "paged/p5", "paged/p6"), names(second));
        assertEquals(1, REQUESTS.size(), "the cursor of page 2 was kept from page 1");

        REQUESTS.clear();
        Page last = page(3);
        assertEquals(List.of("paged/p7"), names(last));
        assertEquals(7, last.getTotal());

        assertTrue(page(9).getList().isEmpty(), "past the last page there is nothing");
    }

    @Test
    void anIdIsOneGet() {
        Page page = page(1, new Condition("qualifiedName", "paged/p5", QueryExpression.EQ));
        assertEquals(List.of("paged/p5"), names(page));
        assertEquals(List.of("/api/v1/namespaces/paged/pods/p5"), REQUESTS);

        Page elsewhere = page(1, new Condition("qualifiedName", "paged/p5", QueryExpression.EQ),
                new Condition("namespace", "other", QueryExpression.EQ));
        assertTrue(elsewhere.getList().isEmpty(), "outside the asked namespace it is not there");
    }

    @Test
    void anExactSearchOnASelectableFieldStaysPaged() {
        page(1, new Condition("phase", "Running", QueryExpression.EQ));
        assertEquals(1, REQUESTS.size());
        String request = URLDecoder.decode(REQUESTS.get(0), StandardCharsets.UTF_8);
        assertTrue(request.contains("fieldSelector=status.phase=Running") && request.contains("limit=3"), request);
    }

    @Test
    void otherFiltersStillWorkInMemory() {
        Page running = page(1, new Condition("phase", "unn", QueryExpression.LIKE));
        assertEquals(7, running.getTotal(), "a fuzzy match the API server cannot apply reads the whole list");
        assertTrue(REQUESTS.get(0).contains("limit=1000"), REQUESTS.get(0));
    }

}
