package xyz.erupt.decision.answer;

/**
 * Token usage of one evaluation. Output tokens are billed at zero by TypeSafe today, but the
 * count is reported so a caller can meter it anyway.
 *
 * @author YuePeng
 */
public record Usage(int inputTokens, int outputTokens) {
}
