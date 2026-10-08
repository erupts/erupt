package xyz.erupt.ai_tune.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * Side-by-side answers of the base and the tuned model to one prompt.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Getter
@Setter
public class CompareVo {

    private Answer base;

    private Answer tuned;

    @Getter
    @Setter
    public static class Answer {

        private String model;

        private String text;

        private String error;

        private long millis;

    }

}
