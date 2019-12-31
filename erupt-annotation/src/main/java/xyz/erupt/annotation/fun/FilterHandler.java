package xyz.erupt.annotation.fun;

/**
 * @author liyuepeng
 * @date 2018-11-05.
 */
public interface FilterHandler {

    /**
     * 数据过滤
     *
     * @param condition 过滤字符串
     * @param params    注解参数
     * @return
     */
    String filter(String condition, String[] params);
}
