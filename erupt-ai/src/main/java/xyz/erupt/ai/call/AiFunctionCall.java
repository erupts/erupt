package xyz.erupt.ai.call;

/**
 * @author YuePeng
 * date 2025/3/14 23:30
 */
public interface AiFunctionCall {

    default String code() {
        return this.getClass().getSimpleName();
    }

    String description();

    String call(String userPrompt);

}
