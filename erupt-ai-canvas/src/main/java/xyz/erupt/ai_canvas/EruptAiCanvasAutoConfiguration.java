package xyz.erupt.ai_canvas;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.model.AiCanvasVersion;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2026/8/3
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
@Component
public class EruptAiCanvasAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiCanvasAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-canvas").description("AI generated HTML view pages backed by the Erupt REST API").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$ai-canvas", "AI Canvas", "fa fa-television", 27));
        menus.add(MetaMenu.createEruptClassMenu(AiCanvas.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(AiCanvasVersion.class, menus.get(0), 20));
        return menus;
    }

}
