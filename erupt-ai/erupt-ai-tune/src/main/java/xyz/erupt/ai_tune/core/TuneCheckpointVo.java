package xyz.erupt.ai_tune.core;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * An intermediate model the provider kept during training.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class TuneCheckpointVo {

    private String id;

    private Integer step;

    // Model id usable for inference, when the provider serves checkpoints
    private String model;

    private Map<String, Double> metrics;

}
