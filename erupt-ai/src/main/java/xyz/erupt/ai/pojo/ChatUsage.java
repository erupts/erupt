package xyz.erupt.ai.pojo;

import lombok.Data;

/**
 * @author YuePeng
 * date 2025/2/25 23:15
 */
@Data
public class ChatUsage {

    private int prompt_tokens = 0;

    private int completion_tokens = 0;

    private int total_tokens = 0;

    public ChatUsage plus(ChatUsage chatUsage) {
        this.completion_tokens += chatUsage.completion_tokens;
        this.prompt_tokens += chatUsage.prompt_tokens;
        this.total_tokens += chatUsage.total_tokens;
        return this;
    }

}
