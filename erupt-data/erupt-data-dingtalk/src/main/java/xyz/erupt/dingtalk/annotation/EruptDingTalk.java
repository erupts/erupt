package xyz.erupt.dingtalk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a DingTalk Notable sheet. Place alongside
 * {@code @EruptDataProcessor(EruptDingTalkDataService.DATA_PROCESSOR)}.
 * <p>
 * Credentials (app key / secret) never live here — they are bound from Spring
 * configuration under {@code erupt.dingtalk.*}. This annotation only names the
 * non-secret identifiers of the target sheet.
 * <p>
 * Records are read from and written to
 * {@code /v1.0/notable/bases/{baseId}/sheets/{sheetIdOrName}/records}. Each
 * record's {@code id} maps to the model's primary-key field (see
 * {@code @Erupt.primaryKeyCol}), so it must be declared on the model for edit /
 * delete / drill to address a row; it is left empty on add and filled by
 * DingTalk. Query mode is LOCAL: the full sheet is fetched (cursor-paged) and
 * then filtered / sorted / paged in memory, suited to config / dictionary scale
 * data.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptDingTalk {

    /**
     * The Notable base identifier ({@code baseId}), taken from the document URL.
     */
    String baseId();

    /**
     * The sheet identifier or its display name within the base.
     */
    String sheet();

    /**
     * Union id of the user every API call is made on behalf of. Empty falls back
     * to {@code erupt.dingtalk.operator-id}.
     */
    String operatorId() default "";

}
