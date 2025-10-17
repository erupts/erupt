package xyz.erupt.ai.pojo;

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
     * Sampling temperature to use, between 0 and 1.
     * Higher values (e.g., 0.7) make the output more random,
     * while lower values (e.g., 0.2) make it more focused and deterministic.
     */
    private Float temperature;

    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass.
     * So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     * We generally recommend altering this or temperature, but not both.
     */
    private Float topP;

    /**
     * Maximum number of tokens to generate in the completion.
     * The total length of input tokens and generated tokens is limited by the model's context length.
     */
    private Integer maxTokens;

    /**
     * Number between -2.0 and 2.0.
     * Positive values penalize new tokens based on their existing frequency in the text so far,
     * decreasing the model's likelihood to repeat the same line verbatim.
     */
    private Float frequency_penalty;

    /**
     * Number between -2.0 and 2.0.
     * Positive values penalize new tokens if they have already appeared in the text,
     * increasing the model's likelihood to talk about new topics.
     */
    private Float presence_penalty;

    /**
     * An object specifying the format that the model must output.
     * Set to { "type": "json_object" } to enable JSON mode, which guarantees the generated message is valid JSON.
     * Note: When using JSON mode, you must still instruct the model to generate JSON via system or user messages,
     * otherwise it may produce endless whitespace until the token limit is reached, making the request appear "stuck".
     * Additionally, if finish_reason="length", the message content may be partially truncated because generation exceeded max_tokens or the conversation exceeded the maximum context length.
     */
    private Map<String, String> response_format;

    /**
     * List of stop sequences; the API will stop generating further tokens when any of these is encountered.
     */
    private List<String> stop;

}
