package xyz.erupt.ai_tune.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_tune.constants.EventLevel;
import xyz.erupt.ai_tune.constants.TuneMethod;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.core.*;
import xyz.erupt.ai_tune.model.TuneJob;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Together AI: {@code /v1/files/upload} and {@code /v1/fine-tunes}. Together takes an absolute
 * learning rate, so the multiplier scales its documented default of 1e-5. Open-weight models
 * train as LoRA adapters, which Together can serve without a dedicated deployment.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class TogetherTune extends TuneCore {

    private static final double DEFAULT_LEARNING_RATE = 1e-5;

    @Override
    public String[] codes() {
        return new String[]{"Together"};
    }

    @Override
    public String uploadFile(TuneJob job, String fileName, byte[] jsonl, boolean validation) {
        JsonObject res = postMultipart(job, baseUrl(job) + "/v1/files/upload",
                Map.of("purpose", "fine-tune", "file_name", fileName), "file", fileName, jsonl);
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
        if (null != job.getEpochs()) body.addProperty("n_epochs", job.getEpochs());
        if (null != job.getBatchSize()) body.addProperty("batch_size", job.getBatchSize());
        if (null != job.getLearningRateMultiplier()) body.addProperty("learning_rate", DEFAULT_LEARNING_RATE * job.getLearningRateMultiplier());
        JsonObject method = new JsonObject();
        if (TuneMethod.DPO.equals(job.getMethod())) {
            method.addProperty("method", "dpo");
            if (null != job.getDpoBeta()) method.addProperty("dpo_beta", job.getDpoBeta());
        } else {
            method.addProperty("method", "sft");
        }
        body.add("training_method", method);
        JsonObject lora = new JsonObject();
        lora.addProperty("type", "Lora");
        body.add("training_type", lora);
        JsonObject res = postJson(job, baseUrl(job) + "/v1/fine-tunes", body);
        String id = str(res, "id");
        if (StringUtils.isBlank(id)) throw new IllegalStateException("Job creation returned no id: " + res);
        return id;
    }

    @Override
    public TuneJobState retrieve(TuneJob job) {
        JsonObject res = getJson(job, baseUrl(job) + "/v1/fine-tunes/" + job.getRemoteJobId());
        TuneJobState state = new TuneJobState();
        state.setStatus(mapStatus(str(res, "status")));
        state.setFineTunedModel(str(res, "output_name"));
        state.setTrainedTokens(lng(res, "token_count"));
        if (TuneStatus.FAILED.equals(state.getStatus())) {
            // The failure reason is the last error-level event
            JsonArray events = res.has("events") && res.get("events").isJsonArray() ? res.getAsJsonArray("events") : new JsonArray();
            for (int i = events.size() - 1; i >= 0; i--) {
                JsonObject ev = events.get(i).getAsJsonObject();
                if ("error".equalsIgnoreCase(str(ev, "level"))) {
                    state.setError(str(ev, "message"));
                    break;
                }
            }
        }
        return state;
    }

    static String mapStatus(String status) {
        if (null == status) return TuneStatus.QUEUED;
        return switch (status.toLowerCase()) {
            case "pending", "queued" -> TuneStatus.QUEUED;
            case "running", "compressing", "uploading", "cancel_requested" -> TuneStatus.RUNNING;
            case "completed" -> TuneStatus.SUCCEEDED;
            case "error" -> TuneStatus.FAILED;
            case "cancelled", "canceled" -> TuneStatus.CANCELLED;
            default -> TuneStatus.RUNNING;
        };
    }

    @Override
    public List<TuneEventVo> events(TuneJob job, Set<String> knownIds) {
        JsonObject res = getJson(job, baseUrl(job) + "/v1/fine-tunes/" + job.getRemoteJobId() + "/events");
        List<TuneEventVo> fresh = new ArrayList<>();
        if (!res.has("data") || !res.get("data").isJsonArray()) return fresh;
        int index = 0;
        for (JsonElement el : res.getAsJsonArray("data")) {
            JsonObject ev = el.getAsJsonObject();
            // Together events carry no id: hash plus position keeps a stable identity across polls
            String id = StringUtils.defaultIfBlank(str(ev, "hash"), "ev-" + index + "-" + lng(ev, "created_at"));
            index++;
            if (knownIds.contains(id)) continue;
            TuneEventVo vo = new TuneEventVo();
            vo.setRemoteId(id);
            vo.setCreatedAt(epochSeconds(lng(ev, "created_at")));
            String level = str(ev, "level");
            vo.setLevel(null != level && level.toLowerCase().startsWith("err") ? EventLevel.ERROR
                    : null != level && level.toLowerCase().startsWith("warn") ? EventLevel.WARN : EventLevel.INFO);
            vo.setMessage(StringUtils.defaultIfBlank(str(ev, "message"), str(ev, "type")));
            vo.setStep(integer(ev, "step"));
            vo.setTrainLoss(dbl(ev, "training_loss"));
            vo.setValidLoss(dbl(ev, "eval_loss"));
            LogMetrics.apply(vo);
            fresh.add(vo);
        }
        return fresh;
    }

    @Override
    public List<TuneCheckpointVo> checkpoints(TuneJob job) {
        JsonObject res = getJson(job, baseUrl(job) + "/v1/fine-tunes/" + job.getRemoteJobId() + "/checkpoints");
        List<TuneCheckpointVo> list = new ArrayList<>();
        if (!res.has("data") || !res.get("data").isJsonArray()) return list;
        for (JsonElement el : res.getAsJsonArray("data")) {
            JsonObject cp = el.getAsJsonObject();
            TuneCheckpointVo vo = new TuneCheckpointVo();
            vo.setId(str(cp, "name"));
            vo.setStep(integer(cp, "step"));
            vo.setModel(str(cp, "name"));
            vo.setMetrics(new LinkedHashMap<>());
            list.add(vo);
        }
        return list;
    }

    @Override
    public void cancel(TuneJob job) {
        postEmpty(job, baseUrl(job) + "/v1/fine-tunes/" + job.getRemoteJobId() + "/cancel");
    }

}
