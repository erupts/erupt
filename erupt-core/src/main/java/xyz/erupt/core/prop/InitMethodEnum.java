package xyz.erupt.core.prop;

/**
 * 初始化方式
 *
 * @author YuePeng
 * date 2022/7/11 22:35
 */
public enum InitMethodEnum {

    NONE,    //不进行初始化
    EVERY,   //每次都进行初始化
    FILE     //通过文件是否存在判断

}
