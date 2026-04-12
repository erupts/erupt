package xyz.erupt.ai.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/3/7 00:00
 */
@Getter
@Setter
@NoArgsConstructor
public class SseBody {

    private SseEvent event = SseEvent.TOKEN;

    private String data;

    public SseBody(SseEvent even, String data) {
        this.data = data;
        this.event = even;
    }
}
