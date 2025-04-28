package xyz.erupt.ai.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/25 23:02
 */
@Data
public class ChatCompletionResponse {

    private String id;

    private String object;

    private long created;

    private String model;

    private List<Choice> choices;

    private ChatUsage usage;

    @Data
    public static class Choice {

        private int index;

        private Message message;

        private String finishReason;

    }

    @Data
    public static class Message {

        private String role;

        private String content;

    }

    public String getMessageStr() {
        StringBuilder sb = new StringBuilder();
        for (Choice choice : this.choices) {
            sb.append(choice.getMessage().getContent());
        }
        return sb.toString();
    }

}
