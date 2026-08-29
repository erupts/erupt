package xyz.erupt.ai_rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.service.EmbeddingModelService;
import xyz.erupt.ai_rag.constants.DocStatus;
import xyz.erupt.ai_rag.core.RetrievedChunk;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.model.KnowledgeBase;
import xyz.erupt.ai_rag.model.KnowledgeChunk;
import xyz.erupt.ai_rag.model.KnowledgeDocument;
import xyz.erupt.ai_rag.prop.VectorStoreProp;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.jpa.dao.EruptDao;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vector store orchestration: store cache with hot-swap eviction, async
 * document ingestion and semantic retrieval.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Service
@Slf4j
public class RagService {

    private static final String COLLECTION_PREFIX = "erupt_kb_";

    // Bound memory during ingestion and keep failure blast radius small
    private static final int EMBED_BATCH_SIZE = 32;

    private static final int ERROR_INFO_MAX_LENGTH = 500;

    private final Map<Long, EmbeddingStore<TextSegment>> storeCache = new ConcurrentHashMap<>();

    @Resource
    private EmbeddingModelService embeddingModelService;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private VectorStoreProp vectorStoreProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    public static String collectionName(Long kbId) {
        return COLLECTION_PREFIX + kbId;
    }

    public EmbeddingStore<TextSegment> store(KnowledgeBase kb) {
        return storeCache.computeIfAbsent(kb.getId(), id ->
                storeCore().connect(vectorStoreProp, collectionName(kb.getId()), kb.getEmbeddingLlm().getDimension()));
    }

    private VectorStoreCore storeCore() {
        VectorStoreCore core = VectorStoreCore.resolve(vectorStoreProp.getType());
        if (null == core) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("rag.unknown_store") + " " + vectorStoreProp.getType());
        }
        return core;
    }

    // Surface store misconfiguration at startup instead of on first ingestion; never blocks boot
    @EventListener(ApplicationReadyEvent.class)
    public void vectorStoreStartupCheck() {
        try {
            VectorStoreCore core = this.storeCore();
            core.healthCheck(vectorStoreProp);
            log.info("RAG vector store ready: {}", core.code());
        } catch (Exception e) {
            log.warn("RAG vector store health check failed: {}", e.getMessage());
        }
    }

    public void evictStore(Long kbId) {
        storeCache.remove(kbId);
    }

    /**
     * Parse → split → embed → upsert, with a PENDING → EMBEDDING → READY/FAILED
     * status machine committed step by step so progress is visible in the table.
     */
    @Async
    public void ingestAsync(Long docId) {
        KnowledgeDocument doc = loadDocumentWithRetry(docId);
        if (null == doc) {
            log.warn("Knowledge document {} not found, ingestion skipped", docId);
            return;
        }
        // The UPDATE below blocks on the row lock until the caller's transaction
        // commits, so the reload afterwards is guaranteed to see the new content
        this.updateDocument(docId, d -> {
            d.setStatus(DocStatus.EMBEDDING);
            d.setErrorInfo(null);
        });
        doc = Optional.ofNullable(eruptDao.lambdaQuery(KnowledgeDocument.class)
                .eq(KnowledgeDocument::getId, docId).one()).orElse(doc);
        try {
            KnowledgeBase kb = doc.getKb();
            EmbeddingModel model = embeddingModelService.get(kb.getEmbeddingLlm());
            EmbeddingStore<TextSegment> store = this.store(kb);
            this.clearChunks(doc, store);
            String content = this.resolveContent(doc);
            List<TextSegment> segments = dev.langchain4j.data.document.splitter.DocumentSplitters
                    .recursive(kb.getChunkSize(), kb.getChunkOverlap()).split(Document.from(content));
            int seq = 0;
            List<KnowledgeChunk> chunks = new ArrayList<>(segments.size());
            for (int from = 0; from < segments.size(); from += EMBED_BATCH_SIZE) {
                List<TextSegment> batch = segments.subList(from, Math.min(from + EMBED_BATCH_SIZE, segments.size()));
                List<Embedding> embeddings = model.embedAll(batch).content();
                List<String> vectorIds = store.addAll(embeddings, batch);
                for (int i = 0; i < batch.size(); i++) {
                    KnowledgeChunk chunk = new KnowledgeChunk();
                    chunk.setDocument(doc);
                    chunk.setSeq(++seq);
                    chunk.setText(batch.get(i).text());
                    chunk.setVectorId(vectorIds.get(i));
                    chunks.add(chunk);
                }
            }
            transactionTemplate.executeWithoutResult(status -> chunks.forEach(eruptDao::persist));
            this.updateDocument(docId, d -> {
                d.setStatus(DocStatus.READY);
                d.setChunkCount(chunks.size());
                d.setErrorInfo(null);
            });
        } catch (Exception e) {
            log.error("Knowledge document {} ingestion failed", docId, e);
            this.updateDocument(docId, d -> {
                d.setStatus(DocStatus.FAILED);
                d.setErrorInfo(StringUtils.abbreviate(Optional.ofNullable(e.getMessage())
                        .orElseGet(() -> ExceptionUtils.getRootCauseMessage(e)), ERROR_INFO_MAX_LENGTH));
            });
        }
    }

    public List<RetrievedChunk> retrieve(KnowledgeBase kb, String query, Integer topK, Double minScore) {
        Embedding queryEmbedding = embeddingModelService.get(kb.getEmbeddingLlm()).embed(query).content();
        EmbeddingSearchResult<TextSegment> result = this.store(kb).search(EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(null == topK ? kb.getTopK() : topK)
                .minScore(null == minScore ? kb.getMinScore() : minScore)
                .build());
        if (result.matches().isEmpty()) {
            return List.of();
        }
        List<String> vectorIds = result.matches().stream().map(m -> m.embeddingId()).collect(Collectors.toList());
        // Chunk text lives in the database (single source of truth), look it up by vector id
        Map<String, KnowledgeChunk> chunkByVectorId = eruptDao.lambdaQuery(KnowledgeChunk.class)
                .in(KnowledgeChunk::getVectorId, vectorIds).list()
                .stream().collect(Collectors.toMap(KnowledgeChunk::getVectorId, Function.identity(), (a, b) -> a));
        return result.matches().stream().map(match -> {
            KnowledgeChunk chunk = chunkByVectorId.get(match.embeddingId());
            return RetrievedChunk.builder()
                    .chunkId(null == chunk ? null : chunk.getId())
                    .document(null == chunk ? null : chunk.getDocument().getName())
                    .seq(null == chunk ? null : chunk.getSeq())
                    .text(null == chunk ? (null == match.embedded() ? null : match.embedded().text()) : chunk.getText())
                    .score(match.score())
                    .build();
        }).collect(Collectors.toList());
    }

    // Re-embed an edited chunk: replace the old vector so text and vector never drift
    public void reEmbedChunk(KnowledgeChunk chunk) {
        KnowledgeChunk persisted = eruptDao.find(KnowledgeChunk.class, chunk.getId());
        KnowledgeBase kb = persisted.getDocument().getKb();
        String oldVectorId = persisted.getVectorId();
        eruptDao.detach(persisted);
        EmbeddingStore<TextSegment> store = this.store(kb);
        Embedding embedding = embeddingModelService.get(kb.getEmbeddingLlm()).embed(chunk.getText()).content();
        if (StringUtils.isNotBlank(oldVectorId)) {
            store.remove(oldVectorId);
        }
        chunk.setVectorId(store.add(embedding, TextSegment.from(chunk.getText())));
    }

    public void removeChunkVector(KnowledgeChunk chunk) {
        KnowledgeChunk persisted = eruptDao.find(KnowledgeChunk.class, chunk.getId());
        if (null == persisted || StringUtils.isBlank(persisted.getVectorId())) {
            return;
        }
        this.store(persisted.getDocument().getKb()).remove(persisted.getVectorId());
    }

    // Delete a document's vectors and chunk rows (used on document delete and re-ingest)
    public void removeDocumentData(KnowledgeDocument doc) {
        this.clearChunks(doc, this.store(doc.getKb()));
    }

    public void dropKnowledgeBase(KnowledgeBase kb) {
        this.storeCore().dropCollection(vectorStoreProp, collectionName(kb.getId()));
        this.evictStore(kb.getId());
    }

    private void clearChunks(KnowledgeDocument doc, EmbeddingStore<TextSegment> store) {
        List<String> vectorIds = eruptDao.lambdaQuery(KnowledgeChunk.class)
                .eq(KnowledgeChunk::getDocument, doc).list()
                .stream().map(KnowledgeChunk::getVectorId).filter(Objects::nonNull).collect(Collectors.toList());
        if (!vectorIds.isEmpty()) {
            store.removeAll(vectorIds);
        }
        transactionTemplate.executeWithoutResult(status -> eruptDao.getEntityManager()
                .createQuery("delete from KnowledgeChunk where document.id = :docId")
                .setParameter("docId", doc.getId()).executeUpdate());
    }

    @SneakyThrows
    private String resolveContent(KnowledgeDocument doc) {
        if (StringUtils.isNotBlank(doc.getAttachment())) {
            AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
            if (null != attachmentProxy && !attachmentProxy.isLocalSave()) {
                // remote-only storage: the file never lands on local disk, fetch it from the attachment domain
                try (InputStream in = URI.create(attachmentProxy.fileDomain() + doc.getAttachment()).toURL().openStream()) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            Path uploadRoot = Paths.get(eruptProp.getUploadPath()).toAbsolutePath().normalize();
            Path file = uploadRoot.resolve(StringUtils.removeStart(doc.getAttachment(), "/")).normalize();
            // Reject any path escaping the upload directory
            if (!file.startsWith(uploadRoot)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("rag.illegal_attachment_path"));
            }
            return Files.readString(file, StandardCharsets.UTF_8);
        }
        if (StringUtils.isBlank(doc.getContent())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("rag.empty_content"));
        }
        return doc.getContent();
    }

    private void updateDocument(Long docId, java.util.function.Consumer<KnowledgeDocument> mutator) {
        transactionTemplate.executeWithoutResult(status -> {
            KnowledgeDocument doc = eruptDao.find(KnowledgeDocument.class, docId);
            if (null != doc) {
                mutator.accept(doc);
                eruptDao.merge(doc);
            }
        });
    }

    // The async task can outrun the caller's transaction commit, retry briefly
    @SneakyThrows
    private KnowledgeDocument loadDocumentWithRetry(Long docId) {
        for (int i = 0; i < 6; i++) {
            KnowledgeDocument doc = eruptDao.lambdaQuery(KnowledgeDocument.class)
                    .eq(KnowledgeDocument::getId, docId).one();
            if (null != doc) {
                return doc;
            }
            Thread.sleep(500);
        }
        return null;
    }

}
