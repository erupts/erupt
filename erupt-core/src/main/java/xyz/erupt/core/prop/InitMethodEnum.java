package xyz.erupt.core.prop;

/**
 * Initialization method
 *
 * @author YuePeng
 * date 2022/7/11 22:35
 */
public enum InitMethodEnum {

    NONE,    // Do not execute the initialization code
    EVERY,   // Initialization is performed every time the system is started.
    FILE     // Determine whether initialization is required by checking the identification file.

}
