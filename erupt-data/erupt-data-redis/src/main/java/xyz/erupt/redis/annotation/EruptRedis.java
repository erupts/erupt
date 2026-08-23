package xyz.erupt.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a set of Redis hashes. Place alongside
 * {@code @EruptDataProcessor(EruptRedisDataService.DATA_PROCESSOR)}.
 * <p>
 * Each row is one hash stored at {@code value() + <primary key>}; model fields map
 * to hash fields. Models should be flat — nested objects are stored as JSON strings.
 * The connection is configured with the standard Spring Boot
 * {@code spring.data.redis.*} properties.
 * <p>
 * The primary key value doubles as the key suffix, so it must be supplied when
 * adding a row (it is not auto-generated).
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptRedis {

    /**
     * Key prefix, e.g. "config:"
     */
    String value();

}
