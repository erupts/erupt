package xyz.erupt.ai_tune.core;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One provider event, normalised. Metrics are optional: most events are plain messages.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class TuneEventVo {

    private String remoteId;

    private LocalDateTime createdAt;

    // One of EventLevel
    private String level;

    private String message;

    private Integer step;

    private Double trainLoss;

    private Double validLoss;

    private Double trainAccuracy;

}
