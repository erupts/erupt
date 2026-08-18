package xyz.erupt.ai_rag.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.core.RetrievedChunk;
import xyz.erupt.ai_rag.model.KnowledgeBase;
import xyz.erupt.ai_rag.service.RagService;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.prompt.SystemPromptProvider;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * Agentic RAG entry point: exposed through the AI toolbox so agents decide on
 * their own when to consult a knowledge base, and role-based tool permissions
 * (LLMRole) control who may search what.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@AiToolbox
@Component
public class RagTools implements SystemPromptProvider {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private RagService ragService;

    @PostConstruct
    public void init() {
        SystemPromptProvider.registerProvider(this);
    }

    @Override
    public String getPrompt() {
        return """
                ## Knowledge Bases
                Call `listKnowledgeBases` to see which knowledge bases exist, then
                `searchKnowledgeBase` to semantically retrieve passages relevant to the
                user's question. Prefer retrieved passages over your own memory when the
                question concerns domain or internal knowledge.
                """;
    }

    @Tool("List available knowledge bases with id, name and content description")
    public String listKnowledgeBases() {
        List<KnowledgeBase> kbs = eruptDao.lambdaQuery(KnowledgeBase.class).eq(KnowledgeBase::getEnable, true).list();
        if (kbs.isEmpty()) {
            return "No knowledge base available.";
        }
        StringBuilder sb = new StringBuilder();
        kbs.forEach(kb -> sb.append("id: ").append(kb.getId()).append(" | name: ").append(kb.getName())
                .append(null == kb.getRemark() ? "" : " | about: " + kb.getRemark()).append("\n"));
        return sb.toString();
    }

    @Tool("Semantic search in a knowledge base, returns the most relevant text passages with similarity scores")
    public String searchKnowledgeBase(@P("knowledge base id, from listKnowledgeBases") long kbId,
                                      @P("natural language query") String query) {
        KnowledgeBase kb = eruptDao.lambdaQuery(KnowledgeBase.class)
                .eq(KnowledgeBase::getId, kbId).eq(KnowledgeBase::getEnable, true).one();
        if (null == kb) {
            return "Knowledge base " + kbId + " not found or disabled.";
        }
        List<RetrievedChunk> chunks = ragService.retrieve(kb, query, null, null);
        if (chunks.isEmpty()) {
            return "No relevant passage found.";
        }
        StringBuilder sb = new StringBuilder();
        chunks.forEach(chunk -> sb.append("[score ").append(String.format("%.3f", chunk.getScore()))
                .append(null == chunk.getDocument() ? "" : " | " + chunk.getDocument())
                .append("]\n").append(chunk.getText()).append("\n\n"));
        return sb.toString();
    }

}
