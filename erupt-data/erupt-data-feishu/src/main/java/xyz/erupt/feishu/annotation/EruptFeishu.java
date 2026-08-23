package xyz.erupt.feishu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a Feishu Bitable (多维表格) table. Place alongside
 * {@code @EruptDataProcessor(EruptFeishuDataService.DATA_PROCESSOR)}.
 * <p>
 * Credentials (app id / secret) are never carried here — they live in Spring
 * configuration under {@code erupt.feishu.*}. This annotation only names the
 * non-secret identifiers of the target table.
 * <p>
 * Records are read from and written to
 * {@code /open-apis/bitable/v1/apps/{baseToken}/tables/{tableId}/records}. Each
 * Bitable record's {@code record_id} maps to the model's primary-key field
 * (see {@code @Erupt.primaryKeyCol}), so it must be declared on the model for
 * edit / delete / drill to address a row; it is left empty on add and filled by
 * Feishu. Query mode is LOCAL: the full table is fetched (cursor-paged) and then
 * filtered / sorted / paged in memory, suited to config / dictionary scale data.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptFeishu {

    /**
     * The Bitable base identifier (app_token), e.g. {@code bascnXXXXXXXX}.
     */
    String baseToken();

    /**
     * The table identifier within the base, e.g. {@code tblXXXXXXXX}.
     */
    String tableId();

}
