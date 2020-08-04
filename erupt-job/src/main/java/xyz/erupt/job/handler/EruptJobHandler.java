package xyz.erupt.job.handler;

/**
 * @author liyuepeng
 * @date 2019-12-26
 */
public interface EruptJobHandler {

    /**
     * 任务处理类
     *
     * @param param 页面配置参数
     * @return 执行结果
     * @throws Exception ex
     */
    String exec(String param) throws Exception;

    /**
     * 成功事件回调函数
     *
     * @param result 执行结果
     * @param param  页面配置参数
     */
    default void success(String result, String param) {
    }

    /**
     * 失败事件回调函数
     *
     * @param throwable throw
     * @param param     页面配置参数
     */
    default void error(Throwable throwable, String param) {
    }

}
