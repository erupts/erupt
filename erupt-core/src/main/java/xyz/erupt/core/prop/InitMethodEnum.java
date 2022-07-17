package xyz.erupt.core.prop;

/**
 * 初始化方式
 *
 * @author YuePeng
 * date 2022/7/11 22:35
 */
public enum InitMethodEnum {

    NONE,    //不执行初始化代码
    EVERY,   //每次启动都进行初始化
    FILE     //通过标识文件判断是否需要初始化

}
