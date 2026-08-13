package xyz.erupt.notion.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a Notion database. Place alongside
 * {@code @EruptDataProcessor(EruptNotionDataService.DATA_PROCESSOR)}.
 * <p>
 * The integration token is never carried here — it lives in Spring configuration
 * under {@code erupt.notion.*}. This annotation only names the target database.
 * <p>
 * Pages are read via {@code POST /v1/databases/{databaseId}/query} and written via
 * {@code /v1/pages}. Each page's {@code id} maps to the model's primary-key field
 * (see {@code @Erupt.primaryKeyCol}), so it must be declared for edit / delete /
 * drill; it is left empty on add and filled by Notion. Delete is a soft delete —
 * Notion's API has no hard delete, so the page is archived. Query mode is LOCAL:
 * the database is fetched (cursor-paged) and filtered / sorted / paged in memory,
 * suited to config / dictionary scale data.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptNotion {

    /**
     * The Notion database id (32-char id, with or without dashes).
     */
    String databaseId();

}
