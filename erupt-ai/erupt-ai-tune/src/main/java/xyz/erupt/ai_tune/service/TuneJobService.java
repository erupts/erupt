package xyz.erupt.ai_tune.service;

import com.google.gson.reflect.TypeToken;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMDataProxy;
import xyz.erupt.ai_tune.constants.DatasetFormat;
import xyz.erupt.ai_tune.constants.DatasetStatus;
import xyz.erupt.ai_tune.constants.EventLevel;
import xyz.erupt.ai_tune.constants.TuneMethod;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.core.TuneCheckpointVo;
import xyz.erupt.ai_tune.core.TuneCore;
import xyz.erupt.ai_tune.core.TuneEventVo;
import xyz.erupt.ai_tune.core.TuneJobState;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.TuneEvent;
import xyz.erupt.ai_tune.model.TuneJob;
import xyz.erupt.ai_tune.prop.TuneProp;
import xyz.erupt.ai_tune.vo.CompareVo;
import xyz.erupt.ai_tune.vo.MonitorVo;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Drives a fine-tuning job through its lifecycle and mirrors the provider's view of it.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Service
@Slf4j
public class TuneJobService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TuneProp tuneProp;

    @Resource
    private TuneDatasetService datasetService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // ---------------------------------------------------------------- start

    /** Validates the job and hands the upload + submit over to a background thread */
    public void start(Long jobId) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job) throw new EruptWebApiRuntimeException("Job not found: " + jobId);
        if (!TuneStatus.STARTABLE.contains(job.getStatus())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.not_startable"));
        }
        this.requireCore(job);
        this.checkDataset(job, job.getTrainingDataset(), true);
        if (null != job.getValidationDataset()) this.checkDataset(job, job.getValidationDataset(), false);
        this.update(jobId, it -> {
            it.setStatus(TuneStatus.UPLOADING);
            it.setStartedAt(LocalDateTime.now());
            it.setFinishedAt(null);
            it.setEstimatedFinish(null);
            it.setErrorInfo(null);
            it.setRemoteJobId(null);
            it.setTrainingFileId(null);
            it.setValidationFileId(null);
            it.setFineTunedModel(null);
            it.setTrainedTokens(null);
            it.setCheckpoints(null);
        });
        this.submitAsync(jobId);
    }

    private TuneCore requireCore(TuneJob job) {
        if (null == job.getBaseLlm()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.no_base_llm"));
        TuneCore core = TuneCore.get(job.getBaseLlm().getLlm());
        if (null == core) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.provider_unsupported") + " " + job.getBaseLlm().getLlm()
                    + " (" + String.join(", ", new TreeSet<>(TuneCore.supportedCodes())) + ")");
        }
        return core;
    }

    private void checkDataset(TuneJob job, TuneDataset dataset, boolean training) {
        dataset = eruptDao.find(TuneDataset.class, dataset.getId());
        if (!DatasetStatus.READY.equals(dataset.getStatus())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.dataset_not_ready") + " " + dataset.getName());
        }
        String expected = TuneMethod.DPO.equals(job.getMethod()) ? DatasetFormat.PREFERENCE : DatasetFormat.CHAT;
        if (!expected.equals(dataset.getFormat())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.format_mismatch") + " " + dataset.getName());
        }
        int valid = null == dataset.getValidCount() ? 0 : dataset.getValidCount();
        if (training && valid < tuneProp.getMinSamples()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.too_few_samples") + " " + valid + " < " + tuneProp.getMinSamples());
        }
    }

    @Async
    public void submitAsync(Long jobId) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job) return;
        try {
            TuneCore core = this.requireCore(job);
            TuneDataset training = eruptDao.find(TuneDataset.class, job.getTrainingDataset().getId());
            byte[] trainingJsonl = datasetService.toJsonl(training);
            this.event(job, EventLevel.INFO, "Uploading training set '" + training.getName() + "' (" + training.getValidCount()
                    + " samples, " + trainingJsonl.length / 1024 + " KB)");
            String trainingFileId = core.uploadFile(job, fileName(training), trainingJsonl, false);
            this.update(jobId, it -> it.setTrainingFileId(trainingFileId));
            job.setTrainingFileId(trainingFileId);
            if (null != job.getValidationDataset()) {
                TuneDataset validation = eruptDao.find(TuneDataset.class, job.getValidationDataset().getId());
                byte[] validationJsonl = datasetService.toJsonl(validation);
                this.event(job, EventLevel.INFO, "Uploading validation set '" + validation.getName() + "' (" + validation.getValidCount() + " samples)");
                String validationFileId = core.uploadFile(job, fileName(validation), validationJsonl, true);
                this.update(jobId, it -> it.setValidationFileId(validationFileId));
                job.setValidationFileId(validationFileId);
            }
            String remoteJobId = core.createJob(job);
            this.update(jobId, it -> {
                it.setRemoteJobId(remoteJobId);
                it.setStatus(TuneStatus.VALIDATING);
            });
            this.event(job, EventLevel.INFO, "Submitted to " + job.getBaseLlm().getLlm() + " as job " + remoteJobId
                    + " on base model " + job.getBaseModel());
            this.sync(jobId);
        } catch (Exception e) {
            log.error("Fine-tuning job {} failed to start", jobId, e);
            this.fail(job, e.getMessage());
        }
    }

    private static String fileName(TuneDataset dataset) {
        return "erupt-dataset-" + dataset.getId() + ".jsonl";
    }

    // ---------------------------------------------------------------- sync

    /** Pulls status, new events and (on success) checkpoints from the provider */
    public void sync(Long jobId) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job || StringUtils.isBlank(job.getRemoteJobId())) return;
        TuneCore core = this.requireCore(job);
        TuneJobState state = core.retrieve(job);
        Set<String> known = eruptDao.lambdaQuery(TuneEvent.class).eq(TuneEvent::getJob, job)
                .isNotNull(TuneEvent::getRemoteId).listSelect(TuneEvent::getRemoteId).stream().collect(Collectors.toSet());
        List<TuneEventVo> fresh = core.events(job, known);
        // Oldest first so ids ascend with time
        fresh.sort(Comparator.comparing(TuneEventVo::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        transactionTemplate.executeWithoutResult(status -> {
            for (TuneEventVo vo : fresh) {
                if (known.contains(vo.getRemoteId())) continue;
                TuneEvent event = TuneEvent.of(job, vo.getLevel(), vo.getMessage());
                event.setRemoteId(vo.getRemoteId());
                if (null != vo.getCreatedAt()) event.setCreatedAt(vo.getCreatedAt());
                event.setStep(vo.getStep());
                event.setTrainLoss(vo.getTrainLoss());
                event.setValidLoss(vo.getValidLoss());
                event.setTrainAccuracy(vo.getTrainAccuracy());
                eruptDao.persist(event);
                known.add(vo.getRemoteId());
            }
        });
        boolean justFinished = !TuneStatus.isTerminal(job.getStatus()) && TuneStatus.isTerminal(state.getStatus());
        List<TuneCheckpointVo> checkpoints = TuneStatus.SUCCEEDED.equals(state.getStatus()) ? safeCheckpoints(core, job) : null;
        this.update(jobId, it -> {
            // A locally issued cancel wins over a provider that has not noticed it yet
            if (!(TuneStatus.CANCELLED.equals(it.getStatus()) && !TuneStatus.isTerminal(state.getStatus()))) {
                it.setStatus(state.getStatus());
            }
            if (StringUtils.isNotBlank(state.getFineTunedModel())) it.setFineTunedModel(state.getFineTunedModel());
            if (null != state.getTrainedTokens()) it.setTrainedTokens(state.getTrainedTokens());
            it.setEstimatedFinish(state.getEstimatedFinish());
            if (StringUtils.isNotBlank(state.getError())) it.setErrorInfo(StringUtils.abbreviate(state.getError(), 1900));
            if (TuneStatus.isTerminal(state.getStatus()) && null == it.getFinishedAt()) it.setFinishedAt(LocalDateTime.now());
            if (null != checkpoints) it.setCheckpoints(GsonFactory.getGson().toJson(checkpoints));
        });
        if (justFinished) {
            String level = TuneStatus.SUCCEEDED.equals(state.getStatus()) ? EventLevel.INFO : EventLevel.ERROR;
            String message = switch (state.getStatus()) {
                case TuneStatus.SUCCEEDED -> "Training succeeded, fine-tuned model: " + state.getFineTunedModel();
                case TuneStatus.FAILED -> "Training failed: " + StringUtils.defaultIfBlank(state.getError(), "no reason given by the provider");
                default -> "Training cancelled";
            };
            this.event(job, level, message);
        }
    }

    private static List<TuneCheckpointVo> safeCheckpoints(TuneCore core, TuneJob job) {
        try {
            return core.checkpoints(job);
        } catch (Exception e) {
            log.warn("Checkpoints of job {} unavailable: {}", job.getId(), e.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------------- cancel / register

    public void cancel(Long jobId) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job) return;
        if (!TuneStatus.CANCELLABLE.contains(job.getStatus()) || StringUtils.isBlank(job.getRemoteJobId())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.not_cancellable"));
        }
        this.requireCore(job).cancel(job);
        this.update(jobId, it -> {
            it.setStatus(TuneStatus.CANCELLED);
            it.setFinishedAt(LocalDateTime.now());
        });
        this.event(job, EventLevel.WARN, "Cancel requested by " + Optional.ofNullable(job.getUpdateBy()).orElse("console"));
    }

    /** Creates an LLM record for the tuned model, reusing the base model's connection */
    public LLM register(Long jobId) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job || !TuneStatus.SUCCEEDED.equals(job.getStatus()) || StringUtils.isBlank(job.getFineTunedModel())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.not_registrable"));
        }
        if (null != job.getRegisteredLlm()) return job.getRegisteredLlm();
        LLM base = job.getBaseLlm();
        LLM llm = new LLM();
        llm.setName(job.getName());
        llm.setLlm(base.getLlm());
        llm.setModel(job.getFineTunedModel());
        llm.setApiUrl(base.getApiUrl());
        llm.setApiKey(base.getApiKey());
        llm.setMaxContext(base.getMaxContext());
        llm.setEnable(true);
        llm.setDefaultLLM(false);
        llm.setConfig(StringUtils.defaultIfBlank(base.getConfig(), LLMDataProxy.gson.toJson(LlmCore.getLLM(base).config())));
        llm.setRemark("Fine-tuned from " + job.getBaseModel() + " by job #" + job.getId());
        transactionTemplate.executeWithoutResult(status -> {
            Integer max = (Integer) eruptDao.lambdaQuery(LLM.class).max(LLM::getSort);
            llm.setSort(null == max ? 10 : max + 10);
            eruptDao.persist(llm);
            TuneJob fresh = eruptDao.find(TuneJob.class, jobId);
            fresh.setRegisteredLlm(llm);
            eruptDao.merge(fresh);
        });
        this.event(job, EventLevel.INFO, "Registered as LLM '" + llm.getName() + "'");
        return llm;
    }

    // ---------------------------------------------------------------- compare

    public CompareVo compare(Long jobId, String system, String prompt) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job || StringUtils.isBlank(job.getFineTunedModel())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("tune.not_registrable"));
        }
        List<ChatMessage> messages = new ArrayList<>();
        if (StringUtils.isNotBlank(system)) messages.add(SystemMessage.from(system));
        messages.add(UserMessage.from(prompt));
        CompareVo vo = new CompareVo();
        vo.setBase(ask(job, job.getBaseModel(), messages));
        vo.setTuned(ask(job, job.getFineTunedModel(), messages));
        return vo;
    }

    private static CompareVo.Answer ask(TuneJob job, String model, List<ChatMessage> messages) {
        CompareVo.Answer answer = new CompareVo.Answer();
        answer.setModel(model);
        long start = System.currentTimeMillis();
        try {
            LlmRequest request = job.getBaseLlm().toLlmRequest();
            request.setModel(model);
            ChatModel chatModel = LlmCore.getLLM(job.getBaseLlm()).buildChatModel(request, messages);
            answer.setText(chatModel.chat(messages).aiMessage().text());
        } catch (Exception e) {
            answer.setError(e.getMessage());
        }
        answer.setMillis(System.currentTimeMillis() - start);
        return answer;
    }

    // ---------------------------------------------------------------- monitor

    public MonitorVo monitor(Long jobId) {
        TuneJob job = eruptDao.find(TuneJob.class, jobId);
        if (null == job) throw new EruptWebApiRuntimeException("Job not found: " + jobId);
        MonitorVo vo = new MonitorVo();
        vo.setId(job.getId());
        vo.setName(job.getName());
        vo.setStatus(job.getStatus());
        vo.setProvider(null == job.getBaseLlm() ? null : job.getBaseLlm().getLlm());
        vo.setBaseModel(job.getBaseModel());
        vo.setFineTunedModel(job.getFineTunedModel());
        vo.setMethod(job.getMethod());
        if (null != job.getTrainingDataset()) {
            vo.setTrainingDataset(job.getTrainingDataset().getName());
            vo.setTrainingDatasetId(job.getTrainingDataset().getId());
            vo.setTrainingSamples(job.getTrainingDataset().getValidCount());
            vo.setTrainingTokens(job.getTrainingDataset().getTokenEstimate());
        }
        if (null != job.getValidationDataset()) vo.setValidationDataset(job.getValidationDataset().getName());
        Map<String, Object> hyper = vo.getHyperparameters();
        hyper.put("epochs", job.getEpochs());
        hyper.put("learningRateMultiplier", job.getLearningRateMultiplier());
        hyper.put("batchSize", job.getBatchSize());
        if (TuneMethod.DPO.equals(job.getMethod())) hyper.put("dpoBeta", job.getDpoBeta());
        hyper.put("seed", job.getSeed());
        hyper.put("suffix", job.getSuffix());
        vo.setRemoteJobId(job.getRemoteJobId());
        vo.setTrainedTokens(job.getTrainedTokens());
        vo.setCreateTime(job.getCreateTime());
        vo.setStartedAt(job.getStartedAt());
        vo.setFinishedAt(job.getFinishedAt());
        vo.setEstimatedFinish(job.getEstimatedFinish());
        vo.setErrorInfo(job.getErrorInfo());
        vo.setRegisteredLlm(null == job.getRegisteredLlm() ? null : job.getRegisteredLlm().getName());
        vo.setEvents(eruptDao.lambdaQuery(TuneEvent.class).eq(TuneEvent::getJob, job)
                .orderByAsc(TuneEvent::getCreatedAt).orderByAsc(TuneEvent::getId).list()
                .stream().map(TuneJobService::toVo).collect(Collectors.toList()));
        vo.setCheckpoints(StringUtils.isBlank(job.getCheckpoints()) ? List.of()
                : GsonFactory.getGson().fromJson(job.getCheckpoints(), new TypeToken<List<TuneCheckpointVo>>() {
        }.getType()));
        return vo;
    }

    private static TuneEventVo toVo(TuneEvent event) {
        TuneEventVo vo = new TuneEventVo();
        vo.setRemoteId(event.getRemoteId());
        vo.setCreatedAt(event.getCreatedAt());
        vo.setLevel(event.getLevel());
        vo.setMessage(event.getMessage());
        vo.setStep(event.getStep());
        vo.setTrainLoss(event.getTrainLoss());
        vo.setValidLoss(event.getValidLoss());
        vo.setTrainAccuracy(event.getTrainAccuracy());
        return vo;
    }

    // ---------------------------------------------------------------- helpers

    public void fail(TuneJob job, String reason) {
        this.update(job.getId(), it -> {
            it.setStatus(TuneStatus.FAILED);
            it.setErrorInfo(StringUtils.abbreviate(reason, 1900));
            it.setFinishedAt(LocalDateTime.now());
        });
        this.event(job, EventLevel.ERROR, StringUtils.abbreviate(reason, 1900));
    }

    public void event(TuneJob job, String level, String message) {
        transactionTemplate.executeWithoutResult(status -> eruptDao.persist(TuneEvent.of(job, level, message)));
    }

    private void update(Long jobId, Consumer<TuneJob> mutator) {
        transactionTemplate.executeWithoutResult(status -> {
            TuneJob job = eruptDao.find(TuneJob.class, jobId);
            if (null != job) {
                mutator.accept(job);
                eruptDao.merge(job);
            }
        });
    }

}
