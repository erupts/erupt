package xyz.erupt.ai_tune.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_tune.constants.EventLevel;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.core.LogMetrics;
import xyz.erupt.ai_tune.core.TuneCore;
import xyz.erupt.ai_tune.core.TuneEventVo;
import xyz.erupt.ai_tune.core.TuneJobState;
import xyz.erupt.ai_tune.model.TuneJob;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Alibaba Cloud Model Studio (DashScope) fine-tuning: {@code /api/v1/files} for uploads and
 * {@code /api/v1/fine-tunes} for jobs. Progress arrives as plain log lines, so metrics are
 * parsed from the text.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class QwenTune extends TuneCore {

    private static final int LOG_LINES = 2000;

    @Override
    public String[] codes() {
        return new String[]{"Qwen"};
    }

    @Override
    public String uploadFile(TuneJob job, String fileName, byte[] jsonl, boolean validation) {
        JsonObject res = postMultipart(job, baseUrl(job) + "/api/v1/files",
                Map.of("descriptions", validation ? "erupt validation set" : "erupt training set"), "files", fileName, jsonl);
        JsonObject data = obj(res, "data");
        if (null != data && data.has("uploaded_files") && data.get("uploaded_files").isJsonArray()
                && data.getAsJsonArray("uploaded_files").size() > 0) {
            String id = str(data.getAsJsonArray("uploaded_files").get(0).getAsJsonObject(), "file_id");
            if (StringUtils.isNotBlank(id)) return id;
        }
        throw new IllegalStateException("File upload returned no file id: " + res);
    }

    @Override
    public String createJob(TuneJob job) {
        JsonObject body = new JsonObject();
        body.addProperty("model", job.getBaseModel());
        JsonArray training = new JsonArray();
        training.add(job.getTrainingFileId());
        body.add("training_file_ids", training);
        if (null != job.getValidationFileId()) {
            JsonArray validation = new JsonArray();
            validation.add(job.getValidationFileId());
            body.add("validation_file_ids", validation);
        }
        body.addProperty("training_type", "efficient_sft");
        JsonObject hyper = new JsonObject();
        if (null != job.getEpochs()) hyper.addProperty("n_epochs", job.getEpochs());
        if (null != job.getBatchSize()) hyper.addProperty("batch_size", job.getBatchSize());
        if (null != job.getLearningRateMultiplier()) hyper.addProperty("learning_rate", String.valueOf(1.6e-5 * job.getLearningRateMultiplier()));
        if (hyper.size() > 0) body.add("hyper_parameters", hyper);
        JsonObject res = postJson(job, baseUrl(job) + "/api/v1/fine-tunes", body);
        String id = str(obj(res, "output"), "job_id");
        if (StringUtils.isBlank(id)) throw new IllegalStateException("Job creation returned no job id: " + res);
        return id;
    }

    @Override
    public TuneJobState retrieve(TuneJob job) {
        JsonObject res = getJson(job, baseUrl(job) + "/api/v1/fine-tunes/" + job.getRemoteJobId());
        JsonObject output = obj(res, "output");
        TuneJobState state = new TuneJobState();
        state.setStatus(mapStatus(str(output, "status")));
        state.setFineTunedModel(str(output, "finetuned_output"));
        JsonObject usage = obj(output, "usage");
        if (null != usage) state.setTrainedTokens(lng(usage, "total_tokens"));
        if (TuneStatus.FAILED.equals(state.getStatus())) {
            state.setError(StringUtils.defaultIfBlank(str(output, "message"), str(res, "message")));
        }
        return state;
    }

    static String mapStatus(String status) {
        if (null == status) return TuneStatus.QUEUED;
        return switch (status.toUpperCase()) {
            case "PENDING", "QUEUING", "QUEUED" -> TuneStatus.QUEUED;
            case "RUNNING" -> TuneStatus.RUNNING;
            case "SUCCEEDED", "SUCCESS" -> TuneStatus.SUCCEEDED;
            case "FAILED" -> TuneStatus.FAILED;
            case "CANCELED", "CANCELLED" -> TuneStatus.CANCELLED;
            default -> TuneStatus.RUNNING;
        };
    }

    @Override
    public List<TuneEventVo> events(TuneJob job, Set<String> knownIds) {
        // Log lines are positional: start after the last one already mirrored
        int offset = knownIds.stream().filter(it -> it.startsWith("log-"))
                .mapToInt(it -> Integer.parseInt(it.substring(4)) + 1).max().orElse(0);
        JsonObject res = getJson(job, baseUrl(job) + "/api/v1/fine-tunes/" + job.getRemoteJobId()
                + "/logs?offset=" + offset + "&line=" + LOG_LINES);
        JsonObject output = obj(res, "output");
        List<TuneEventVo> fresh = new ArrayList<>();
        if (null == output || !output.has("logs") || !output.get("logs").isJsonArray()) return fresh;
        int index = offset;
        for (JsonElement el : output.getAsJsonArray("logs")) {
            String line = el.isJsonPrimitive() ? el.getAsString() : el.toString();
            TuneEventVo vo = new TuneEventVo();
            vo.setRemoteId("log-" + index++);
            vo.setCreatedAt(LocalDateTime.now());
            String lower = line.toLowerCase();
            vo.setLevel(lower.contains("error") || lower.contains("exception") ? EventLevel.ERROR
                    : lower.contains("warn") ? EventLevel.WARN : EventLevel.INFO);
            vo.setMessage(StringUtils.abbreviate(line, 2000));
            LogMetrics.apply(vo);
            fresh.add(vo);
        }
        return fresh;
    }

    @Override
    public void cancel(TuneJob job) {
        postEmpty(job, baseUrl(job) + "/api/v1/fine-tunes/" + job.getRemoteJobId() + "/cancel");
    }

}
