package xyz.erupt.decision.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.decision.constant.DecisionConst;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.service.DecisionService;

/**
 * Decisions over HTTP, for callers that are not this JVM. Both endpoints answer in the
 * provider's documented shape and keep the provider key on the server.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + DecisionConst.API)
public class DecisionController {

    @Resource
    private DecisionService decisionService;

    /**
     * Runs a decision declared in the admin. Body: <code>{"state": …}</code>, where the state is
     * a string, an object or an array.
     */
    @PostMapping("/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public JsonObject run(@PathVariable("code") String code, @RequestBody JsonObject body,
                          @RequestParam(name = "model", required = false) String model) {
        JsonElement state = body.get("state");
        if (null == state || state.isJsonNull()) throw new EruptWebApiRuntimeException("state is required");
        return decisionService.rawRun(code, state, this.model(model));
    }

    /**
     * Asks questions the caller writes itself, in the provider's request shape. Everything but
     * the endpoint and the key comes from the caller.
     */
    @PostMapping
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public JsonObject ask(@RequestBody JsonObject request,
                          @RequestParam(name = "model", required = false) String model) {
        if (!request.has("state")) throw new EruptWebApiRuntimeException("state is required");
        if (!request.has("questions")) throw new EruptWebApiRuntimeException("questions is required");
        DecisionModel config = this.model(model);
        return decisionService.raw(null == config ? decisionService.defaultModel() : config, request);
    }

    // Names the decision model row to use; blank falls back to the default one
    private DecisionModel model(String name) {
        return StringUtils.isBlank(name) ? null : decisionService.model(name);
    }

}
