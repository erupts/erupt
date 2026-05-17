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
@Comment("Intelligently checks whether serialization is needed based on field override status; the current algorithm only supports single-level field overrides")
public @interface EruptSmartSkipSerialize {

}
