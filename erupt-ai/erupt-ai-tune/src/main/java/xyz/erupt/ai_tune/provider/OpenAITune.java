package xyz.erupt.ai_tune.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_tune.constants.EventLevel;
import xyz.erupt.ai_tune.constants.TuneMethod;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.core.TuneCheckpointVo;
import xyz.erupt.ai_tune.core.TuneCore;
import xyz.erupt.ai_tune.core.TuneEventVo;
import xyz.erupt.ai_tune.core.TuneJobState;
import xyz.erupt.ai_tune.model.TuneJob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The OpenAI fine-tuning protocol: {@code /files} for uploads and {@code /fine_tuning/jobs}
 * for the job, its events and checkpoints. Several hosted providers and most self-hosted
 * trainers speak it, so concrete adapters only pick the path prefix and the request dialect.
 *
 * @author YuePeng
 * date 2026/10/8
 */
public abstract class OpenAITune extends TuneCore {

    private static final int EVENT_PAGE = 100;

    private static final int MAX_EVENT_PAGES = 20;

    protected String apiPoint() {
        return "/v1";
    }

    /**
     * Whether hyperparameters travel in the current {@code method} block or in the legacy
     * top-level {@code hyperparameters} object most compatible servers still expect
     */
    protected boolean methodBlock() {
        return true;
    }

    protected String api(TuneJob job, String path) {
        return baseUrl(job) + apiPoint() + path;
    }

    @Override
    public String uploadFile(TuneJob job, String fileName, byte[] jsonl, boolean validation) {
        JsonObject res = postMultipart(job, api(job, "/files"), Map.of("purpose", "fine-tune"), "file", fileName, jsonl);
        String id = str(res, "id");
        if (StringUtils.isBlank(id)) throw new IllegalStateException("File upload returned no id: " + res);
        return id;
    }

    @Override
    public String createJob(TuneJob job) {
        JsonObject body = new JsonObject();
        body.addProperty("model", job.getBaseModel());
        body.addProperty("training_file", job.getTrainingFileId());
        if (null != job.getValidationFileId()) body.addProperty("validation_file", job.getValidationFileId());
        if (StringUtils.isNotBlank(job.getSuffix())) body.addProperty("suffix", job.getSuffix());
        if (null != job.getSeed()) body.addProperty("seed", job.getSeed());
        JsonObject hyper = new JsonObject();
        if (null != job.getEpochs()) hyper.addProperty("n_epochs", job.getEpochs());
        if (null != job.getLearningRateMultiplier()) hyper.addProperty("learning_rate_multiplier", job.getLearningRateMultiplier());
        if (null != job.getBatchSize()) hyper.addProperty("batch_size", job.getBatchSize());
        if (methodBlock()) {
            boolean dpo = TuneMethod.DPO.equals(job.getMethod());
            if (dpo && null != job.getDpoBeta()) hyper.addProperty("beta", job.getDpoBeta());
            JsonObject inner = new JsonObject();
            inner.add("hyperparameters", hyper);
            JsonObject method = new JsonObject();
            method.addProperty("type", dpo ? "dpo" : "supervised");
            method.add(dpo ? "dpo" : "supervised", inner);
            body.add("method", method);
        } else if (hyper.size() > 0) {
            body.add("hyperparameters", hyper);
        }
        JsonObject res = postJson(job, api(job, "/fine_tuning/jobs"), body);
        String id = str(res, "id");
        if (StringUtils.isBlank(id)) throw new IllegalStateException("Job creation returned no id: " + res);
        return id;
    }

    @Override
    public TuneJobState retrieve(TuneJob job) {
        JsonObject res = getJson(job, api(job, "/fine_tuning/jobs/" + job.getRemoteJobId()));
        TuneJobState state = new TuneJobState();
        state.setStatus(mapStatus(str(res, "status")));
        state.setFineTunedModel(str(res, "fine_tuned_model"));
        state.setTrainedTokens(lng(res, "trained_tokens"));
        state.setEstimatedFinish(epochSeconds(lng(res, "estimated_finish")));
        JsonObject error = obj(res, "error");
        if (null != error) state.setError(str(error, "message"));
        return state;
    }

    static String mapStatus(String status) {
        if (null == status) return TuneStatus.QUEUED;
        return switch (status.toLowerCase()) {
            case "validating_files", "validating" -> TuneStatus.VALIDATING;
            case "queued", "pending", "created" -> TuneStatus.QUEUED;
            case "running", "training" -> TuneStatus.RUNNING;
            case "succeeded", "success", "completed", "finished" -> TuneStatus.SUCCEEDED;
            case "failed", "error" -> TuneStatus.FAILED;
            case "cancelled", "canceled" -> TuneStatus.CANCELLED;
            default -> TuneStatus.RUNNING;
        };
    }

    @Override
    public List<TuneEventVo> events(TuneJob job, Set<String> knownIds) {
        List<TuneEventVo> fresh = new ArrayList<>();
        String after = null;
        // Newest first; walk back until a page touches an event already mirrored
        for (int page = 0; page < MAX_EVENT_PAGES; page++) {
            String url = api(job, "/fine_tuning/jobs/" + job.getRemoteJobId() + "/events?limit=" + EVENT_PAGE
                    + (null == after ? "" : "&after=" + after));
            JsonObject res = getJson(job, url);
            JsonArray data = res.has("data") && res.get("data").isJsonArray() ? res.getAsJsonArray("data") : new JsonArray();
            boolean reachedKnown = false;
            for (JsonElement el : data) {
                JsonObject ev = el.getAsJsonObject();
                String id = str(ev, "id");
                if (null != id && knownIds.contains(id)) {
                    reachedKnown = true;
                    continue;
                }
                fresh.add(toEvent(ev));
                after = id;
            }
            if (reachedKnown || data.size() < EVENT_PAGE || !res.has("has_more") || !res.get("has_more").getAsBoolean()) break;
        }
        return fresh;
    }

    private static TuneEventVo toEvent(JsonObject ev) {
        TuneEventVo vo = new TuneEventVo();
        vo.setRemoteId(str(ev, "id"));
        vo.setCreatedAt(epochSeconds(lng(ev, "created_at")));
        String level = str(ev, "level");
        vo.setLevel(null == level ? EventLevel.INFO : level.toUpperCase().startsWith("ERR") ? EventLevel.ERROR
                : level.toUpperCase().startsWith("WARN") ? EventLevel.WARN : EventLevel.INFO);
        vo.setMessage(str(ev, "message"));
        JsonObject data = obj(ev, "data");
        if (null != data) {
            vo.setStep(integer(data, "step"));
            vo.setTrainLoss(dbl(data, "train_loss"));
            Double valid = dbl(data, "valid_loss");
            vo.setValidLoss(null != valid ? valid : dbl(data, "full_valid_loss"));
            vo.setTrainAccuracy(dbl(data, "train_mean_token_accuracy"));
        }
        return vo;
    }

    @Override
    public List<TuneCheckpointVo> checkpoints(TuneJob job) {
        JsonObject res = getJson(job, api(job, "/fine_tuning/jobs/" + job.getRemoteJobId() + "/checkpoints"));
        List<TuneCheckpointVo> list = new ArrayList<>();
        if (!res.has("data") || !res.get("data").isJsonArray()) return list;
        for (JsonElement el : res.getAsJsonArray("data")) {
            JsonObject cp = el.getAsJsonObject();
            TuneCheckpointVo vo = new TuneCheckpointVo();
            vo.setId(str(cp, "id"));
            vo.setStep(integer(cp, "step_number"));
            vo.setModel(str(cp, "fine_tuned_model_checkpoint"));
            vo.setMetrics(numericFields(obj(cp, "metrics")));
            list.add(vo);
        }
        list.sort((a, b) -> Integer.compare(null == a.getStep() ? 0 : a.getStep(), null == b.getStep() ? 0 : b.getStep()));
        return list;
    }

    @Override
    public void cancel(TuneJob job) {
        postEmpty(job, api(job, "/fine_tuning/jobs/" + job.getRemoteJobId() + "/cancel"));
    }

}
