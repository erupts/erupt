package xyz.erupt.ai.handler;

/**
 * @author YuePeng
 * date 2025/3/1 19:14
 */
public interface EruptPromptHandler {

    default String name() {
        return this.getClass().getSimpleName();
    }

    String handle(String prompt);

}
