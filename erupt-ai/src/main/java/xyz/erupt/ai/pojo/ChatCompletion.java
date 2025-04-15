package xyz.erupt.ai.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/2/25 21:45
 */
@Getter
@Setter
@Builder
public class ChatCompletion {

    private String model;

    private List<ChatCompletionMessage> messages;

    @Builder.Default
    private boolean stream = true;

    /**
     * 使用什么采样温度，介于 0 和 1 之间。较高的值（如 0.7）将使输出更加随机，而较低的值（如 0.2）将使其更加集中和确定性
     */
    @Builder.Default
    private Float temperature = 0F;

    /**
     * 作为调节采样温度的替代方案，模型会考虑前 top_p 概率的 token 的结果。所以 0.1 就意味着只有包括在最高 10% 概率中的 token 会被考虑。
     * 我们通常建议修改这个值或者更改 temperature，但不建议同时对两者进行修改。
     */
    @Builder.Default
    private Float topP = 0.7F;

    /**
     * 限制一次请求中模型生成 completion 的最大 token 数。输入 token 和输出 token 的总长度受模型的上下文长度的限制。
     */
    private Integer maxTokens;

    /**
     * 介于 -2.0 和 2.0 之间的数字。如果该值为正，那么新 token 会根据其在已有文本中的出现频率受到相应的惩罚，降低模型重复相同内容的可能性。
     */
    @Builder.Default
    private Float frequency_penalty = 0F;

    /**
     * 介于 -2.0 和 2.0 之间的数字。如果该值为正，那么新 token 会根据其是否已在已有文本中出现受到相应的惩罚，从而增加模型谈论新主题的可能性。
     */
    @Builder.Default
    private Float presence_penalty = 0F;

    /**
     * 一个 object，指定模型必须输出的格式。
     * 设置为 { "type": "json_object" } 以启用 JSON 模式，该模式保证模型生成的消息是有效的 JSON。
     * 注意: 使用 JSON 模式时，你还必须通过系统或用户消息指示模型生成 JSON。
     * 否则，模型可能会生成不断的空白字符，直到生成达到令牌限制，从而导致请求长时间运行并显得“卡住”。
     * 此外，如果 finish_reason="length"，这表示生成超过了 max_tokens 或对话超过了最大上下文长度，消息内容可能会被部分截断。
     */
    private Map<String, String> response_format;

    /**
     * 在遇到这些词时，API 将停止生成更多的 token。
     */
    private List<String> stop;


    /**
     * 辅助属性
     */
    @JsonIgnore
    private List<String> functions;

}
