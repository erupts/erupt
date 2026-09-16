package xyz.erupt.cloud.server.interceptor;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comments belong to the server's own database, so a build model coming back from a node must never
 * advertise a comment stream — otherwise the browser would fire comment calls that get forwarded to
 * a node with no erupt-comment module.
 *
 * @author YuePeng
 */
class EruptCloudServerInterceptorTest {

    private EruptBuildModel nodeBuildModel(String eruptName) {
        EruptBuildModel buildModel = new EruptBuildModel();
        EruptModel eruptModel = new EruptModel(eruptName, "node");
        JsonObject power = new JsonObject();
        power.addProperty("comment", true);
        power.addProperty("add", true);
        JsonObject eruptJson = new JsonObject();
        eruptJson.add("power", power);
        eruptModel.setEruptJson(eruptJson);
        buildModel.setEruptModel(eruptModel);
        buildModel.setPower(new PowerObject());
        return buildModel;
    }

    @Test
    void commentIsTurnedOffInBothViewsOfPower() {
        EruptBuildModel buildModel = this.nodeBuildModel("Node1");
        EruptCloudServerInterceptor.disableComment(buildModel);
        assertFalse(buildModel.getPower().isComment());
        assertFalse(buildModel.getEruptModel().getEruptJson().getAsJsonObject("power").get("comment").getAsBoolean());
        // unrelated permissions stay as the node declared them
        assertTrue(buildModel.getPower().isAdd());
        assertTrue(buildModel.getEruptModel().getEruptJson().getAsJsonObject("power").get("add").getAsBoolean());
    }

    @Test
    void tabEruptsAreCoveredToo() {
        EruptBuildModel buildModel = this.nodeBuildModel("Node1");
        EruptBuildModel tab = this.nodeBuildModel("Node1Tab");
        buildModel.setTabErupts(Collections.singletonMap("tab", tab));
        EruptCloudServerInterceptor.disableComment(buildModel);
        assertFalse(tab.getPower().isComment());
        assertFalse(tab.getEruptModel().getEruptJson().getAsJsonObject("power").get("comment").getAsBoolean());
    }

    @Test
    void missingPowerIsTolerated() {
        EruptBuildModel buildModel = new EruptBuildModel();
        EruptModel eruptModel = new EruptModel("Node1", "node");
        eruptModel.setEruptJson(new JsonObject());
        buildModel.setEruptModel(eruptModel);
        EruptCloudServerInterceptor.disableComment(buildModel);
    }

}
