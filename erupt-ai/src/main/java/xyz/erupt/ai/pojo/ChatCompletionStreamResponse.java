package xyz.erupt.ai.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/25 23:02
 */
@Data
public class ChatCompletionStreamResponse {

    private String id;

    private String object;

    private long created;

    private String model;

    private List<Choice> choices;

    private String systemFingerprint;

    @Data
    public static class Choice {
        private int index;

        private Delta delta;

        private String finishReason;

        private ChatUsage usage;
    }

    @Data
    public static class Delta {

        private String content;

    }

}
