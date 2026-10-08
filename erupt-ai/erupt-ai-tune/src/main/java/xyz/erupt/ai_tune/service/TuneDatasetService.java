package xyz.erupt.ai_tune.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.model.AiChatMessage;
import xyz.erupt.ai_tune.constants.DatasetFormat;
import xyz.erupt.ai_tune.constants.DatasetStatus;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.TuneSample;
import xyz.erupt.ai_tune.model.input.ChatHarvestForm;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.jpa.dao.EruptDao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Builds, validates and serialises training datasets.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Service
@Slf4j
public class TuneDatasetService {

    public static final String SOURCE_FILE = "FILE";

    public static final String SOURCE_CHAT = "CHAT";

    public static final String SOURCE_MANUAL = "MANUAL";

    private static final int BATCH = 200;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    /** Re-imports the attached file (replacing earlier file samples), then validates everything */
    @Async
    public void ingestAsync(Long datasetId) {
        this.ingest(datasetId);
    }

    public void ingest(Long datasetId) {
        TuneDataset dataset = eruptDao.find(TuneDataset.class, datasetId);
        if (null == dataset) return;
        this.update(datasetId, it -> {
            it.setStatus(DatasetStatus.VALIDATING);
            it.setErrorInfo(null);
        });
        try {
            if (StringUtils.isNotBlank(dataset.getAttachment())) {
                List<String> lines = readLines(dataset);
                transactionTemplate.executeWithoutResult(status -> eruptDao.getEntityManager()
                        .createQuery("delete from TuneSample where dataset.id = :id and source = :source")
                        .setParameter("id", datasetId).setParameter("source", SOURCE_FILE).executeUpdate());
                this.appendSamples(dataset, lines, SOURCE_FILE);
            }
            this.revalidate(datasetId);
        } catch (Exception e) {
            log.error("Dataset {} ingestion failed", datasetId, e);
            this.update(datasetId, it -> {
                it.setStatus(DatasetStatus.FAILED);
                it.setErrorInfo(StringUtils.abbreviate(e.getMessage(), 1900));
            });
        }
    }

    /** Validates every sample again and refreshes the dataset totals */
    public void revalidate(Long datasetId) {
        TuneDataset dataset = eruptDao.find(TuneDataset.class, datasetId);
        if (null == dataset) return;
        List<Long> ids = eruptDao.lambdaQuery(TuneSample.class).eq(TuneSample::getDataset, dataset)
                .orderByAsc(TuneSample::getSeq).listSelect(TuneSample::getId);
        for (int from = 0; from < ids.size(); from += BATCH) {
            List<Long> slice = ids.subList(from, Math.min(ids.size(), from + BATCH));
            transactionTemplate.executeWithoutResult(status -> {
                for (TuneSample sample : eruptDao.lambdaQuery(TuneSample.class).in(TuneSample::getId, slice).list()) {
                    this.apply(sample, dataset.getFormat());
                    eruptDao.merge(sample);
                }
            });
        }
        this.refreshStats(datasetId);
    }

    /** Validates one sample in place, filling valid / turns / tokens / errorInfo */
    public void apply(TuneSample sample, String format) {
        SampleValidator.Result result = SampleValidator.validate(format, sample.getContent());
        sample.setValid(result.isValid());
        sample.setErrorInfo(result.getError());
        sample.setTurns(result.getTurns());
        sample.setTokens(result.getTokens());
    }

    /** Recomputes counts, token estimate and status from the stored samples */
    public void refreshStats(Long datasetId) {
        transactionTemplate.executeWithoutResult(status -> {
            TuneDataset dataset = eruptDao.find(TuneDataset.class, datasetId);
            if (null == dataset) return;
            Object[] row = (Object[]) eruptDao.getEntityManager().createQuery(
                            "select count(s), sum(case when s.valid = true then 1 else 0 end), "
                                    + "sum(case when s.valid = true then s.tokens else 0 end) "
                                    + "from TuneSample s where s.dataset.id = :id")
                    .setParameter("id", datasetId).getSingleResult();
            long total = ((Number) row[0]).longValue();
            long valid = null == row[1] ? 0 : ((Number) row[1]).longValue();
            long tokens = null == row[2] ? 0 : ((Number) row[2]).longValue();
            dataset.setSampleCount((int) total);
            dataset.setValidCount((int) valid);
            dataset.setTokenEstimate(tokens);
            if (total == 0) {
                dataset.setStatus(DatasetStatus.PENDING);
                dataset.setErrorInfo(null);
            } else if (valid == 0) {
                dataset.setStatus(DatasetStatus.FAILED);
                dataset.setErrorInfo(firstError(dataset));
            } else {
                dataset.setStatus(DatasetStatus.READY);
                dataset.setErrorInfo(valid == total ? null : (total - valid) + " of " + total + " samples are invalid and will be skipped: " + firstError(dataset));
            }
            eruptDao.merge(dataset);
        });
    }

    private String firstError(TuneDataset dataset) {
        TuneSample bad = eruptDao.lambdaQuery(TuneSample.class).eq(TuneSample::getDataset, dataset)
                .eq(TuneSample::getValid, false).orderByAsc(TuneSample::getSeq).limit(1).one();
        return null == bad ? "" : "#" + bad.getSeq() + " " + bad.getErrorInfo();
    }

    /** Appends raw JSONL lines as samples; blank lines are skipped */
    public int appendSamples(TuneDataset dataset, List<String> lines, String source) {
        int[] seq = {nextSeq(dataset)};
        List<String> content = lines.stream().map(String::strip).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        for (int from = 0; from < content.size(); from += BATCH) {
            List<String> slice = content.subList(from, Math.min(content.size(), from + BATCH));
            transactionTemplate.executeWithoutResult(status -> {
                for (String line : slice) {
                    TuneSample sample = new TuneSample();
                    sample.setDataset(dataset);
                    sample.setSeq(seq[0]++);
                    sample.setSource(source);
                    sample.setContent(line);
                    this.apply(sample, dataset.getFormat());
                    eruptDao.persist(sample);
                }
            });
        }
        return content.size();
    }

    public int nextSeq(TuneDataset dataset) {
        Object max = eruptDao.lambdaQuery(TuneSample.class).eq(TuneSample::getDataset, dataset).max(TuneSample::getSeq);
        return null == max ? 1 : ((Number) max).intValue() + 1;
    }

    /**
     * Turns AI Chat conversations into supervised samples. Only user/model pairs are used;
     * answers produced through tool calls or cut off by the user are dropped.
     */
    public int harvest(TuneDataset dataset, ChatHarvestForm form) {
        if (!DatasetFormat.CHAT.equals(dataset.getFormat())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.harvest_chat_only"));
        }
        List<AiChatMessage> messages = eruptDao.lambdaQuery(AiChatMessage.class)
                .ge(null != form.getFromDate(), AiChatMessage::getCreatedAt, null == form.getFromDate() ? null : form.getFromDate().atStartOfDay())
                .lt(null != form.getToDate(), AiChatMessage::getCreatedAt, null == form.getToDate() ? null : form.getToDate().plusDays(1).atStartOfDay())
                .eq(StringUtils.isNotBlank(form.getModel()), AiChatMessage::getModel, form.getModel())
                .orderByDesc(AiChatMessage::getId).list();
        Map<Long, List<AiChatMessage>> byChat = new LinkedHashMap<>();
        for (AiChatMessage message : messages) {
            if (null == message.getChatId()) continue;
            byChat.computeIfAbsent(message.getChatId(), k -> new ArrayList<>()).add(message);
        }
        int minAnswer = null == form.getMinAnswerLength() ? 0 : form.getMinAnswerLength();
        int maxConversations = null == form.getMaxConversations() ? Integer.MAX_VALUE : form.getMaxConversations();
        List<String> lines = new ArrayList<>();
        int conversations = 0;
        for (List<AiChatMessage> chat : byChat.values()) {
            if (conversations >= maxConversations) break;
            chat.sort(Comparator.comparing(AiChatMessage::getId));
            List<String[]> pairs = new ArrayList<>();
            for (int i = 0; i + 1 < chat.size(); i++) {
                AiChatMessage q = chat.get(i), a = chat.get(i + 1);
                if (q.getSenderType() != ChatSenderType.USER || a.getSenderType() != ChatSenderType.MODEL) continue;
                if (Boolean.TRUE.equals(a.getInterrupted()) || StringUtils.isNotBlank(a.getToolCalls())) continue;
                if (StringUtils.isBlank(q.getContent()) || StringUtils.isBlank(a.getContent()) || a.getContent().length() < minAnswer) continue;
                pairs.add(new String[]{q.getContent(), a.getContent()});
                i++;
            }
            if (pairs.isEmpty()) continue;
            conversations++;
            if (Boolean.TRUE.equals(form.getPerTurn())) {
                for (int end = 1; end <= pairs.size(); end++) {
                    lines.add(toChatLine(form.getSystemPrompt(), pairs.subList(0, end)));
                }
            } else {
                lines.add(toChatLine(form.getSystemPrompt(), pairs));
            }
        }
        int added = this.appendSamples(dataset, lines, SOURCE_CHAT);
        if (added > 0) this.refreshStats(dataset.getId());
        return added;
    }

    private static String toChatLine(String systemPrompt, List<String[]> pairs) {
        JsonArray messages = new JsonArray();
        if (StringUtils.isNotBlank(systemPrompt)) messages.add(message("system", systemPrompt));
        for (String[] pair : pairs) {
            messages.add(message("user", pair[0]));
            messages.add(message("assistant", pair[1]));
        }
        JsonObject line = new JsonObject();
        line.add("messages", messages);
        return line.toString();
    }

    private static JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    /** Valid samples as JSONL, the body uploaded to the provider and offered for download */
    public byte[] toJsonl(TuneDataset dataset) {
        List<String> lines = eruptDao.lambdaQuery(TuneSample.class).eq(TuneSample::getDataset, dataset)
                .eq(TuneSample::getValid, true).orderByAsc(TuneSample::getSeq).listSelect(TuneSample::getContent);
        // Collapse pretty-printed edits back to one line per sample
        return lines.stream().map(it -> it.replace("\r", "").replace("\n", "")).collect(Collectors.joining("\n"))
                .getBytes(StandardCharsets.UTF_8);
    }

    public void deleteSamples(Long datasetId) {
        transactionTemplate.executeWithoutResult(status -> eruptDao.getEntityManager()
                .createQuery("delete from TuneSample where dataset.id = :id").setParameter("id", datasetId).executeUpdate());
    }

    @SneakyThrows
    private List<String> readLines(TuneDataset dataset) {
        String text;
        AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
        if (null != attachmentProxy && !attachmentProxy.isLocalSave()) {
            // Remote-only storage: the file never lands on local disk, fetch it from the attachment domain
            try (InputStream in = EruptUtil.attachmentUrl(attachmentProxy, dataset.getAttachment()).openStream()) {
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            Path uploadRoot = Paths.get(eruptProp.getUploadPath()).toAbsolutePath().normalize();
            Path file = uploadRoot.resolve(StringUtils.removeStart(dataset.getAttachment(), "/")).normalize();
            // Reject any path escaping the upload directory
            if (!file.startsWith(uploadRoot)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.illegal_attachment_path"));
            }
            text = Files.readString(file, StandardCharsets.UTF_8);
        }
        text = text.strip();
        // A whole JSON array is accepted too and split into lines
        if (text.startsWith("[")) {
            JsonArray array = com.google.gson.JsonParser.parseString(text).getAsJsonArray();
            List<String> lines = new ArrayList<>(array.size());
            array.forEach(el -> lines.add(el.toString()));
            return lines;
        }
        return Arrays.asList(text.split("\\r?\\n"));
    }

    private void update(Long datasetId, Consumer<TuneDataset> mutator) {
        transactionTemplate.executeWithoutResult(status -> {
            TuneDataset dataset = eruptDao.find(TuneDataset.class, datasetId);
            if (null != dataset) {
                mutator.accept(dataset);
                dataset.setUpdateTime(LocalDateTime.now());
                eruptDao.merge(dataset);
            }
        });
    }

}
