package xyz.erupt.atlas.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.atlas.constant.AtlasConstant;
import xyz.erupt.atlas.service.EruptAtlasService;
import xyz.erupt.atlas.vo.AtlasView;
import xyz.erupt.atlas.vo.ModelDetail;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.service.EruptUserService;

/**
 * The atlas as AI tools: how the models hang together, which a schema read one model at a time
 * never shows. Structure only, no rows — and open to whoever may open the atlas page.
 *
 * @author YuePeng
 */
@AiToolbox
@Component
public class AtlasTools {

    @Resource
    private EruptAtlasService eruptAtlasService;

    @Resource
    private EruptUserService eruptUserService;

    @Tool("The model atlas as a graph. nodes: every erupt model, cube and remote model with its table and field count. "
            + "edges: the relations between them (reference, tab, embed, drill, operation, cubeOf, join, table). "
            + "audit: dependency cycles, tables shared by several models, models nothing points at and models without a menu. "
            + "Use it to find which models relate to one another before querying or changing data.")
    public String atlasGraph() {
        eruptUserService.requireMenu(AtlasConstant.MENU_ATLAS);
        AtlasView view = eruptAtlasService.build();
        // text is the page's own translations, of no use to an agent
        return GsonFactory.getGson().toJson(new AtlasView(view.nodes(), view.edges(), view.audit(), null));
    }

    @Tool("Everything the registry knows about one model: table, primary key, powers, data proxies and every field "
            + "with the model a reference field points at; for a cube, its dimensions and measures.")
    public String atlasModelDetail(@P("Node id from atlasGraph: the erupt name, or cube#Name for a cube") String model) {
        eruptUserService.requireMenu(AtlasConstant.MENU_ATLAS);
        ModelDetail detail = eruptAtlasService.detail(model);
        if (null == detail) throw new EruptWebApiRuntimeException("No such model: " + model);
        return GsonFactory.getGson().toJson(detail);
    }

    @Tool("Permission audit of every model: the powers it declares next to the function buttons its menu actually "
            + "offers, and the power handler plugged in. A power without a button is a permission nobody can be granted.")
    public String atlasPowerAudit() {
        eruptUserService.requireMenu(AtlasConstant.MENU_ATLAS);
        return GsonFactory.getGson().toJson(eruptAtlasService.power());
    }

}
