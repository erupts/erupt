package xyz.erupt.airtable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to an Airtable table. Place alongside
 * {@code @EruptDataProcessor(EruptAirtableDataService.DATA_PROCESSOR)}.
 * <p>
 * The personal access token never lives here — it is bound from Spring
 * configuration under {@code erupt.airtable.*}. This annotation only names the
 * non-secret identifiers of the target table.
 * <p>
 * Records are read from and written to {@code /v0/{baseId}/{table}}. Each
 * record's {@code id} ({@code recXXXX}) maps to the model's primary-key field
 * (see {@code @Erupt.primaryKeyCol}), so it must be declared on the model for
 * edit / delete / drill to address a row; it is left empty on add and filled by
 * Airtable. Query mode is LOCAL: the full table is fetched (offset-paged) and
 * then filtered / sorted / paged in memory, suited to config / dictionary scale
 * data.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptAirtable {

    /**
     * The base identifier, e.g. {@code appXXXXXXXXXXXXXX}.
     */
    String baseId();

    /**
     * The table identifier ({@code tblXXXXXXXXXXXXXX}) or its display name.
     */
    String table();

}
