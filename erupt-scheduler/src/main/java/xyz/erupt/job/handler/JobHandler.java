package xyz.erupt.job.handler;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
public interface JobHandler {

    void exec(String param) throws Exception;

    void complete(boolean success, String param);

}
