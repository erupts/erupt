package xyz.erupt.test.cloud;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.controller.EruptFileController;
import xyz.erupt.core.controller.EruptModifyController;
import xyz.erupt.core.controller.EruptTabController;
import xyz.erupt.core.controller.vis.EruptBoardController;
import xyz.erupt.core.controller.vis.EruptCalendarController;
import xyz.erupt.core.controller.vis.EruptGanttController;
import xyz.erupt.excel.controller.EruptExcelController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every endpoint that writes data names the rights it needs: erupt-cloud-server enforces them before it
 * forwards to a node, so an endpoint without them would let any user holding the menu write.
 */
public class EruptRouterPowerTest {

    // read-only endpoints of the controllers below, allowed without a right beyond the menu
    private static final List<String> READS = List.of("EruptGanttController.links", "EruptExcelController.formats");

    @Test
    void writingEndpointsDeclareTheirRights() {
        List<String> missing = new ArrayList<>();
        for (Class<?> controller : List.of(EruptModifyController.class, EruptTabController.class, EruptFileController.class,
                EruptGanttController.class, EruptBoardController.class, EruptCalendarController.class, EruptExcelController.class)) {
            for (Method method : controller.getDeclaredMethods()) {
                EruptRouter router = method.getAnnotation(EruptRouter.class);
                if (null == router || router.verifyType() != EruptRouter.VerifyType.ERUPT || !router.cloudProxy()) continue;
                String name = controller.getSimpleName() + "." + method.getName();
                if (router.power().length == 0 && !READS.contains(name) && null == method.getAnnotation(GetMapping.class)) {
                    missing.add(name);
                }
            }
        }
        assertEquals(List.of(), missing, "endpoints forwarded to nodes without EruptRouter.power");
    }

}
