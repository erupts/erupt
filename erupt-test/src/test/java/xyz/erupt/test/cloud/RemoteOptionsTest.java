package xyz.erupt.test.cloud;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.controller.EruptComponentController;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.test.EruptApplicationTests;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Static options of a node erupt, answered by the server from the node's build: tags from {@code tagsType.tags}
 * when the build carries them, from the {@code componentValue} when it does not (an older erupt, the language SDKs).
 *
 * @author YuePeng
 */
class RemoteOptionsTest extends EruptApplicationTests {

    private static final String NODE = "shop";

    private static final EruptBuildModel BUILD = GsonFactory.getGson().fromJson("""
            {"eruptModel": {"eruptName": "Item", "eruptJson": {"primaryKeyCol": "id"},
              "eruptFieldModels": [
                {"fieldName": "declared", "fieldReturnName": "String", "eruptFieldJson": {"views": [],
                  "edit": {"title": "Declared", "type": "TAGS", "tagsType": {"tags": ["red", "blue"], "fetchHandler": []}}},
                  "componentValue": ["ignored"]},
                {"fieldName": "legacy", "fieldReturnName": "String", "eruptFieldJson": {"views": [],
                  "edit": {"title": "Legacy", "type": "TAGS", "tagsType": {"fetchHandler": []}}},
                  "componentValue": ["small", "large"]},
                {"fieldName": "level", "fieldReturnName": "String", "eruptFieldJson": {"views": [],
                  "edit": {"title": "Level", "type": "CHOICE", "choiceType": {"fetchHandler": []}}},
                  "componentValue": [{"value": "LOW", "label": "Low"}, {"value": "HIGH", "label": "High"}]}
              ]}}""", EruptBuildModel.class);

    @Resource
    private EruptComponentController componentController;

    private EruptRemoteRouter previous;

    @BeforeEach
    void route() {
        previous = EruptRemoteRouterManager.get();
        // only what the server-answered endpoints call
        EruptRemoteRouterManager.register((EruptRemoteRouter) Proxy.newProxyInstance(EruptRemoteRouter.class.getClassLoader(),
                new Class[]{EruptRemoteRouter.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "isRemote" -> ((String) args[0]).startsWith(NODE + ".");
                    case "resolveEruptModel" -> EruptModel.remote((String) args[0], NODE, BUILD);
                    default -> throw new UnsupportedOperationException(method.getName());
                }));
    }

    @AfterEach
    void restore() {
        EruptRemoteRouterManager.register(previous);
    }

    @Test
    void tagsDeclaredInTheBuild() {
        assertEquals(List.of("red", "blue"), componentController.tagsItem(NODE + ".Item", "declared", new JsonObject()));
    }

    @Test
    void tagsOnlyInTheComponentValue() {
        assertEquals(List.of("small", "large"), componentController.tagsItem(NODE + ".Item", "legacy", new JsonObject()));
    }

    @Test
    void choices() {
        List<VLModel> options = componentController.choiceItem(NODE + ".Item", "level");
        assertEquals(List.of("LOW", "HIGH"), options.stream().map(VLModel::getValue).toList());
    }

}
