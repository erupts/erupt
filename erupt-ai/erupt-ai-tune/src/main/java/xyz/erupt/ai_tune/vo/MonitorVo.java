package xyz.erupt.ai_tune.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai_tune.core.TuneCheckpointVo;
import xyz.erupt.ai_tune.core.TuneEventVo;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the job monitor page renders in one round trip.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class MonitorVo {

    private Long id;

    private String name;

    private String status;

    private String provider;

    private String baseModel;

    private String fineTunedModel;

    private String method;

    private String trainingDataset;

    private Long trainingDatasetId;

    private Integer trainingSamples;

    private Long trainingTokens;

    private String validationDataset;

    private Map<String, Object> hyperparameters = new LinkedHashMap<>();

    private String remoteJobId;

    private Long trainedTokens;

    private LocalDateTime createTime;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime estimatedFinish;

    private String errorInfo;

    private String registeredLlm;

    private List<TuneEventVo> events;

    private List<TuneCheckpointVo> checkpoints;

}
