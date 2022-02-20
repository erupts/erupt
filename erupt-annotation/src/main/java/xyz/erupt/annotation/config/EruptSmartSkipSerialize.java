package xyz.erupt.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YuePeng
 * date 2021/12/26 22:42
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Comment("根据字段覆盖情况智能校验是否需要序列化,目前的算法仅支持字段单次覆盖")
public @interface EruptSmartSkipSerialize {

}
