package xyz.erupt.ai_tune.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai.tune")
public class TuneProp {

    // How often active jobs are polled at the provider for status, events and checkpoints
    private Duration syncInterval = Duration.ofSeconds(30);

    // HTTP timeout for one provider call; file uploads of large datasets need headroom
    private Duration requestTimeout = Duration.ofMinutes(5);

    // Fewest valid samples a dataset must hold before a job may start (OpenAI's floor is 10)
    private int minSamples = 10;

}
