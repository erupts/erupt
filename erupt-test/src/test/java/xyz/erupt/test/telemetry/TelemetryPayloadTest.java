package xyz.erupt.test.telemetry;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.telemetry.EruptTelemetry;
import xyz.erupt.upms.telemetry.TelemetryPayload;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Telemetry lives in erupt-upms rather than erupt-core so that erupt-cloud-node, which depends
 * on erupt-core but never on erupt-upms, cannot report at all. This pins the consequence that
 * mattered for that move: the payload built from inside a real context still carries the module
 * list, which is owned by erupt-core.
 * <p>
 * Kept under xyz.erupt.test so @SpringBootTest can find the test application by package search.
 */
public class TelemetryPayloadTest extends EruptApplicationTests {

    @Resource
    private EruptTelemetry eruptTelemetry;

    private TelemetryPayload build(String eventType) throws Exception {
        Method method = EruptTelemetry.class.getDeclaredMethod("buildPayload", String.class, String.class);
        method.setAccessible(true);
        return (TelemetryPayload) method.invoke(eruptTelemetry, "11111111-2222-3333-4444-555555555555", eventType);
    }

    @Test
    public void payloadCarriesTheModuleList() throws Exception {
        TelemetryPayload payload = build("boot");
        assertNotNull(payload.getModules(), "module list must survive the move out of erupt-core");
        assertFalse(payload.getModules().isEmpty(), "module list must not be empty in a real context");
        assertTrue(payload.getModules().contains("erupt-upms"), "the host module must report itself");
    }

    @Test
    public void payloadCarriesHostAndDatabaseFacts() throws Exception {
        TelemetryPayload payload = build("heartbeat");
        assertEquals("heartbeat", payload.getEventType());
        assertNotNull(payload.getEruptVersion());
        assertNotNull(payload.getJavaVersion());
        assertNotNull(payload.getArch());
        assertNotNull(payload.getSpringBootVersion());
        assertNotNull(payload.getTimezone());
        // the test context runs on H2, proving the datasource probe still resolves from upms
        assertNotNull(payload.getDbType());
        assertTrue(payload.getEruptCount() > 0, "registered @Erupt classes must be counted");
    }

}
