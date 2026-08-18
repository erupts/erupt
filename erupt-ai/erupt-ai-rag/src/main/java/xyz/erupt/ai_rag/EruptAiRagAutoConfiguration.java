package xyz.erupt.ai_rag;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.ai_rag.model.KnowledgeBase;
import xyz.erupt.ai_rag.model.KnowledgeChunk;
import xyz.erupt.ai_rag.model.KnowledgeDocument;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Configuration
@ComponentScan
@EntityScan
@EruptScan
public class EruptAiRagAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptAiRagAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-ai-rag")
                .description("Knowledge base and RAG — vector stores, document ingestion and semantic retrieval").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        menus.add(MetaMenu.createRootMenu("$rag", "Knowledge Base", "fa fa-book", 27));
        menus.add(MetaMenu.createEruptClassMenu(KnowledgeBase.class, menus.get(0), 10));
        menus.add(MetaMenu.createEruptClassMenu(KnowledgeDocument.class, menus.get(0), 20));
        menus.add(MetaMenu.createEruptClassMenu(KnowledgeChunk.class, menus.get(0), 30, MenuStatus.HIDE));
        return menus;
    }

}
