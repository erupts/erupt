package xyz.erupt.ai.pojo;

import lombok.Data;

/**
 * @author YuePeng
 * date 2025/2/25 23:15
 */
@Data
public class ChatUsage {

    private int promptTokens = 0;

    private int completionTokens = 0;

    private int totalTokens = 0;

    public ChatUsage plus(ChatUsage chatUsage) {
        this.completionTokens += chatUsage.completionTokens;
        this.promptTokens += chatUsage.promptTokens;
        this.totalTokens += chatUsage.totalTokens;
        return this;
    }

}
