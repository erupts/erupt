package xyz.erupt.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a REST resource. Place alongside
 * {@code @EruptDataProcessor(EruptHttpDataService.DATA_PROCESSOR)}.
 * <p>
 * Expected endpoint shape (JSON):
 * <pre>
 *   GET    {value}        → [ {...}, ... ]  or  { "total": n, "list": [ ... ] }
 *   GET    {value}/{id}   → { ... }
 *   POST   {value}        add
 *   PUT    {value}/{id}   edit
 *   DELETE {value}/{id}   delete
 * </pre>
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptHttp {

    /**
     * Resource base URL, e.g. https://api.example.com/users
     */
    String value();

    /**
     * Extra request headers in "Name: Value" form, e.g. "Authorization: Bearer xxx"
     */
    String[] headers() default {};

    QueryMode queryMode() default QueryMode.LOCAL;

    /**
     * Request timeout in seconds
     */
    int timeout() default 10;

    enum QueryMode {
        /**
         * Fetch the full list once, then filter / sort / page in memory —
         * for endpoints without query capabilities
         */
        LOCAL,
        /**
         * Delegate paging to the endpoint: pageIndex, pageSize, sort and equality
         * conditions are appended as query parameters; response must be
         * { "total": n, "list": [...] } (a plain array is accepted, total = array size)
         */
        REMOTE
    }

}
