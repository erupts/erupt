package xyz.erupt.ai_tune.core;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * What a provider reports about a job on one poll, already mapped to erupt's status set.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class TuneJobState {

    // One of TuneStatus
    private String status;

    private String fineTunedModel;

    private Long trainedTokens;

    private String error;

    private LocalDateTime estimatedFinish;

}
