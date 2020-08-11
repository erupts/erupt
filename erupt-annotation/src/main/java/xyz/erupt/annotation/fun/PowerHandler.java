package xyz.erupt.annotation.fun;

/**
 * @author liyuepeng
 * @date 2019-11-25.
 */
public interface PowerHandler {

    /**
     * erupt权限执行类
     *
     * @param power 注解
     * @return power bean
     */
    void handler(PowerObject power);


}
