package xyz.erupt.ai_canvas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.upms.service.EruptSessionService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The designer reads this marker to tell "a round is running" from "nothing is
 * happening" after a reload, because the stream it was watching is gone by then.
 * The marker therefore lives outside the request, which makes its removal the
 * interesting part: every terminal path clears it, a stop clears it even when the
 * round is orphaned, and a marker left behind by a container that was killed
 * mid-round is treated as dead rather than pinning the designer forever.
 *
 * @author YuePeng
 * date 2026/9/6
 */
class CanvasGeneratingStateTest {

    /** Session store the service actually talks to, with no TTL of its own */
    private static class FakeSession extends EruptSessionService {
        final Map<String, String> store = new HashMap<>();

        @Override
        public void put(String key, String str, long timeout, TimeUnit unit) {
            store.put(key, str);
        }

        @Override
        public Object get(String key) {
            return store.get(key);
        }

        @Override
        public boolean exist(String key) {
            return store.containsKey(key);
        }

        @Override
        public void remove(String key) {
            store.remove(key);
        }
    }

    private static final long CANVAS = 7L;

    private AiCanvasService service;
    private FakeSession session;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        service = new AiCanvasService(List.of());
        session = new FakeSession();
        inject(service, "eruptSessionService", session);
        inject(service, "aiProp", new AiProp());
    }

    private String runningKey() {
        return session.store.keySet().stream().filter(k -> k.contains("generate-running")).findFirst().orElse(null);
    }

    @Test
    void idleCanvasReportsNothing() {
        assertNull(service.generatingState(CANVAS));
    }

    @Test
    void aRunningRoundIsVisibleToALaterRequest() throws Exception {
        mark("build me a table");
        AiCanvasService.GeneratingState state = service.generatingState(CANVAS);
        assertNotNull(state, "a reload must be able to see the round that is running");
        assertEquals("build me a table", state.getMessage());
        assertTrue(state.getStartedAt() > 0);
    }

    @Test
    void heartbeatKeepsTheOriginalStartTime() throws Exception {
        mark("first");
        long startedAt = service.generatingState(CANVAS).getStartedAt();
        Thread.sleep(5);
        beat("first");
        assertEquals(startedAt, service.generatingState(CANVAS).getStartedAt(),
                "the elapsed time the designer shows must not restart on every heartbeat");
    }

    @Test
    void killedContainerLeavesNoStickyState() throws Exception {
        // A Redis-backed session outlives the JVM, so the marker survives a kill -9
        // while the round that wrote it does not. Age alone must retire it.
        mark("orphaned round");
        String key = runningKey();
        AiCanvasService.GeneratingState stale = new AiCanvasService.GeneratingState("orphaned round");
        // A killed container stops beating; that, not the round's age, is what marks it dead
        stale.setBeatAt(System.currentTimeMillis() - (10 * 60 * 1000L));
        session.store.put(key, GsonFactory.getGson().toJson(stale));

        assertNull(service.generatingState(CANVAS), "a marker whose heartbeat lapsed must not pin the designer");
        assertFalse(session.store.containsKey(key), "and it must be dropped, not re-read every poll");
    }

    @Test
    void aLongRoundStaysVisibleWhileItKeepsBeating() throws Exception {
        // Regression: startedAt is held constant across heartbeats, so judging liveness
        // by it reported every round older than the TTL as finished. The designer then
        // announced the round had ended while tokens were still streaming
        mark("a page that takes a while");
        String key = runningKey();
        AiCanvasService.GeneratingState longRun = new AiCanvasService.GeneratingState("a page that takes a while");
        longRun.setStartedAt(System.currentTimeMillis() - (30 * 60 * 1000L));
        session.store.put(key, GsonFactory.getGson().toJson(longRun));

        AiCanvasService.GeneratingState state = service.generatingState(CANVAS);
        assertNotNull(state, "a round running longer than the marker lifetime is still running");
        assertEquals("a page that takes a while", state.getMessage());
        assertTrue(session.store.containsKey(key), "and its marker must survive the read");
    }

    @Test
    void heartbeatRenewsSeveralTimesWithinTheMarkerLifetime() {
        // The marker only lapses on its own while the beat sits well inside the TTL:
        // raise the interval past it and a live round starts looking dead between beats
        assertTrue(AiCanvasService.HEARTBEAT_INTERVAL_MS * 3 <= AiCanvasService.RUNNING_TTL_MS,
                "heartbeat interval must leave room for missed beats within the TTL");
    }

    @Test
    void beatIsThrottledButFiresOnTheInterval() {
        long t = 1_000_000L;
        assertFalse(AiCanvasService.beatDue(t, t), "two events at the same instant beat once");
        assertFalse(AiCanvasService.beatDue(t + AiCanvasService.HEARTBEAT_INTERVAL_MS - 1, t));
        assertTrue(AiCanvasService.beatDue(t + AiCanvasService.HEARTBEAT_INTERVAL_MS, t));
    }

    @Test
    void aMarkerFromAnOlderDeploymentReadsAsDead() {
        // Written before beatAt existed, so it deserializes with beatAt 0
        AiCanvasService.GeneratingState legacy =
                GsonFactory.getGson().fromJson("{\"startedAt\":1,\"message\":\"old\"}", AiCanvasService.GeneratingState.class);
        assertFalse(AiCanvasService.markerAlive(legacy, System.currentTimeMillis()));
        assertFalse(AiCanvasService.markerAlive(null, System.currentTimeMillis()));
    }

    @Test
    void stopAlwaysFreesTheDesigner() throws Exception {
        mark("round the user gave up on");
        service.stopGenerate(CANVAS);
        assertNull(service.generatingState(CANVAS));
    }

    @Test
    void aStoppedRoundIsNotRevivedByLateTokens() throws Exception {
        // Tokens keep arriving after a stop until the model reaches its own end
        mark("round");
        service.stopGenerate(CANVAS);
        beat("round");
        assertNull(service.generatingState(CANVAS), "a late heartbeat must not resurrect a stopped round");
    }

    @Test
    void clearingIsIdempotent() {
        assertDoesNotThrow(() -> service.clearGenerating(CANVAS));
        assertDoesNotThrow(() -> service.clearGenerating(CANVAS));
        assertNull(service.generatingState(CANVAS));
    }

    // --- the two private steps of a round, reached the way generateSse reaches them ---

    private void mark(String message) throws Exception {
        invoke("markGenerating", message);
    }

    private void beat(String message) throws Exception {
        invoke("heartbeatGenerating", message);
    }

    private void invoke(String name, String message) throws Exception {
        var m = AiCanvasService.class.getDeclaredMethod(name, Long.class, String.class);
        m.setAccessible(true);
        m.invoke(service, CANVAS, message);
    }

}
