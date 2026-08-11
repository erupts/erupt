package xyz.erupt.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a database table through plain JDBC. Place alongside
 * {@code @EruptDataProcessor(EruptJdbcDataService.DATA_PROCESSOR)}.
 * <p>
 * Field names are used verbatim as column names; paging relies on
 * {@code LIMIT / OFFSET} (MySQL, PostgreSQL, H2, SQLite ...).
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptJdbc {

    /**
     * Table name
     */
    String value();

    /**
     * DataSource bean name; empty uses the primary DataSource
     */
    String datasource() default "";

}
