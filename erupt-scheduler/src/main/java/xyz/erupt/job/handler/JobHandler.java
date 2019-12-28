package xyz.erupt.job.handler;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
public interface JobHandler {

    String exec(String param) throws Exception;

    default void success(String result, String param) {
    }

    default void error(Throwable throwable, String param) {
    }

}
