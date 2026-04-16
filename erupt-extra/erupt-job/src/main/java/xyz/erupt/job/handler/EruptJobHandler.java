package xyz.erupt.job.handler;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2019-12-26
 */
public interface EruptJobHandler {

    @Comment("Job name")
    default String name() {
        return this.getClass().getSimpleName();
    }

    @Comment("Job execution method body")
    String exec(@Comment("Job code") String code, @Comment("Job parameter") String param);

    default String cron() {
        return "0 0 1 * * ?";
    }

    default String param() {
        return null;
    }

    @Comment("Called when the job executes successfully")
    default void success(@Comment("Execution result") String result, @Comment("Job parameter") String param) {
    }

    @Comment("Called when the job execution fails")
    default void error(@Comment("Exception information") Throwable throwable, @Comment("Job parameter") String param) {
    }

}
