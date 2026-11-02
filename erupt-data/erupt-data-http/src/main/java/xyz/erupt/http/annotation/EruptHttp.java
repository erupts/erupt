package xyz.erupt.http.annotation;

import xyz.erupt.core.view.Page;
import xyz.erupt.http.EruptHttpAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Binds an erupt model to a REST resource. Place alongside
 * {@code @EruptDataProcessor(EruptHttpDataService.DATA_PROCESSOR)}.
 * <p>
 * Expected endpoint shape (JSON):
 * <pre>
 *   GET    {value}        → [ {...}, ... ]  or  { "total": n, "list": [ ... ] }
 *   GET    {value}/{id}   → { ... }
 *   POST   {value}        add
 *   PUT    {value}/{id}   edit  (or PATCH, see {@link #editMethod()})
 *   DELETE {value}/{id}   delete
 * </pre>
 * <p>
 * {@link #value()} and {@link #headers()} are templates, resolved on every request:
 * <ul>
 *   <li>{@code ${erupt.http.token}} — a Spring property, so secrets stay out of source</li>
 *   <li>{@code #{user.tenantId}} — SpEL against the current {@code MetaUser}</li>
 *   <li>{@code #{@tokenService.token()}} — SpEL calling any Spring bean, e.g. a cached OAuth token</li>
 * </ul>
 * Anything the templates and the envelope attributes cannot express goes into an
 * {@link EruptHttpAdapter}.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptHttp {

    /**
     * Resource base URL, e.g. https://api.example.com/users. Template, see class doc.
     */
    String value();

    /**
     * Extra request headers in "Name: Value" form, e.g. "Authorization: Bearer ${gh.token}".
     * Templates, see class doc.
     */
    String[] headers() default {};

    QueryMode queryMode() default QueryMode.LOCAL;

    /**
     * REMOTE mode: how page index and size are named on the query string.
     */
    Paging paging() default Paging.PAGE_SIZE;

    /**
     * Dotted path to the array of records inside a list response, e.g. {@code data.items}.
     * Empty accepts a plain array or a {@code list} property.
     */
    String listPath() default "";

    /**
     * Dotted path to the total record count inside a REMOTE list response, e.g. {@code meta.count}.
     * Empty reads {@code total}, falling back to the array size.
     */
    String totalPath() default "";

    /**
     * Dotted path to the record inside a single-object response, e.g. {@code data}.
     * Empty takes the whole body.
     */
    String itemPath() default "";

    /**
     * HTTP method used for edit.
     */
    EditMethod editMethod() default EditMethod.PUT;

    /**
     * Hooks for everything dynamic: auth, URL rewriting, query encoding, body and
     * response mapping, error extraction. The interface itself means no adapter.
     * A class annotated {@code @Component} is taken from the Spring context, so it can
     * hold injected services and token caches.
     */
    Class<? extends EruptHttpAdapter> adapter() default EruptHttpAdapter.class;

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
         * Delegate paging to the endpoint: paging, sort and equality conditions are
         * appended as query parameters; the response is read through
         * {@link #listPath()} / {@link #totalPath()}
         */
        REMOTE
    }

    enum EditMethod {
        PUT, PATCH
    }

    /**
     * Query-string paging conventions. Each constant knows how to write a {@link Page}
     * into the parameter map; anything else is done in {@link EruptHttpAdapter#query}.
     */
    enum Paging {
        /**
         * {@code pageIndex=1&pageSize=20} (1-based, erupt's own convention)
         */
        PAGE_SIZE {
            @Override
            public void apply(Map<String, String> params, Page page) {
                params.put("pageIndex", String.valueOf(page.getPageIndex()));
                params.put("pageSize", String.valueOf(page.getPageSize()));
            }
        },
        /**
         * {@code page=1&per_page=20} (GitHub, GitLab and most REST APIs)
         */
        PAGE_PER_PAGE {
            @Override
            public void apply(Map<String, String> params, Page page) {
                params.put("page", String.valueOf(page.getPageIndex()));
                params.put("per_page", String.valueOf(page.getPageSize()));
            }
        },
        /**
         * {@code offset=0&limit=20}
         */
        OFFSET_LIMIT {
            @Override
            public void apply(Map<String, String> params, Page page) {
                params.put("offset", String.valueOf((long) (page.getPageIndex() - 1) * page.getPageSize()));
                params.put("limit", String.valueOf(page.getPageSize()));
            }
        };

        public abstract void apply(Map<String, String> params, Page page);
    }

}
