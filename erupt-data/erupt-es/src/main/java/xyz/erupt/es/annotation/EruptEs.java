package xyz.erupt.es.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to an Elasticsearch index. Place alongside
 * {@code @EruptDataProcessor(EruptEsDataService.DATA_PROCESSOR)}.
 * <p>
 * The cluster connection is configured with the standard Spring Boot
 * {@code spring.elasticsearch.*} properties (uris, username, password ...).
 * <p>
 * Equality filtering and sorting use exact (term) semantics — map string fields
 * as {@code keyword} (or use multi-fields) for them to behave as expected.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptEs {

    /**
     * Index name
     */
    String value();

}
