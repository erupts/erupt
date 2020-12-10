package xyz.erupt.annotation.fun;

/**
 * @author liyuepeng
 * @date 2019-11-25.
 */
public interface PowerHandler {

    /**
     * 动态控制各功能使用权限
     *
     * @param power 注解
     * @return power bean
     */
    void handler(PowerObject power);


}
