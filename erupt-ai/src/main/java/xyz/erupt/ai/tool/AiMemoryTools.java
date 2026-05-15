package xyz.erupt.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.model.AiMemory;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.prompt.SystemPromptProvider;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026/5/15
 */
@AiToolbox
@Component
public class AiMemoryTools implements SystemPromptProvider {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        SystemPromptProvider.registerProvider(this);
    }

    @Override
    public String getPrompt() {
        return """
                ## Memory
                Call `listMemories` at the start of each session to restore persisted context.
                During the session, save newly confirmed preferences, decisions, or important facts via `saveMemory`.
                Remove outdated entries with `deleteMemory` to keep memory clean and relevant.
                """;
    }


    @Tool("List all memory entries with ID and a one-line preview. " +
            "Call at the start of each session to restore relevant context.")
    public String listMemories() {
        List<AiMemory> list = eruptDao.lambdaQuery(AiMemory.class)
                .eq(AiMemory::getUserId, MetaContext.getUser().getUid())
                .list();
        if (list.isEmpty()) return "No memories saved yet.";
        return list.stream().map(m -> {
            String preview = m.getContent();
            if (preview != null && preview.length() > 80) preview = preview.substring(0, 80) + "...";
            return m.getId() + ": " + preview;
        }).collect(Collectors.joining("\n"));
    }

    @Tool("Retrieve a specific memory entry by ID.")
    public String getMemory(@P("Memory ID") Long id) {
        AiMemory memory = eruptDao.lambdaQuery(AiMemory.class)
                .eq(AiMemory::getId, id)
                .eq(AiMemory::getUserId, MetaContext.getUser().getUid())
                .one();
        return memory == null ? "Memory not found: " + id : memory.getContent();
    }

    @Tool("Persist information to long-term memory. Call this when the user confirms " +
            "a preference, decision, or context worth retaining across sessions. " +
            "Returns the assigned memory ID.")
    public String saveMemory(@P("Content to remember") String content) {
        AiMemory memory = new AiMemory();
        memory.setContent(content);
        memory.setUserId(MetaContext.getUser().getUid());
        memory.setCreateTime(LocalDateTime.now());
        transactionTemplate.executeWithoutResult(s -> eruptDao.persist(memory));
        return "Memory saved, ID: " + memory.getId();
    }

    @Tool("Delete a memory entry by ID when it is no longer relevant.")
    public String deleteMemory(@P("Memory ID to delete") Long id) {
        AiMemory memory = eruptDao.lambdaQuery(AiMemory.class)
                .eq(AiMemory::getId, id)
                .eq(AiMemory::getUserId, MetaContext.getUser().getUid())
                .one();
        if (memory == null) return "Memory not found: " + id;
        transactionTemplate.executeWithoutResult(s -> eruptDao.delete(memory));
        return "Memory deleted: " + id;
    }

}
