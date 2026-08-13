package xyz.erupt.file.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an erupt model to a data file. Place alongside
 * {@code @EruptDataProcessor(EruptFileDataService.DATA_PROCESSOR)}.
 * <p>
 * The format is inferred from the extension:
 * <ul>
 *   <li>{@code .csv} — CSV whose first line is a header of field names (flat models)</li>
 *   <li>{@code .tsv} — tab-separated values, otherwise identical to CSV</li>
 *   <li>{@code .yml} / {@code .yaml} — YAML; requires SnakeYAML on the classpath</li>
 *   <li>{@code .properties} — Java properties, always a single record</li>
 *   <li>{@code .ini} — INI with {@code [section]} grouping, always a single record;
 *       each section maps to a nested object field</li>
 *   <li>{@code .md} / {@code .markdown} — Markdown with front-matter, always a single
 *       record; the body maps to the {@code content} field</li>
 *   <li>{@code .xml} — XML; a root element wrapping {@code <item>} rows, or the root
 *       itself as one record when {@code single = true}</li>
 *   <li>{@code .jsonl} / {@code .ndjson} — JSON Lines, one object per line, always a list</li>
 *   <li>anything else (e.g. {@code .json}) — JSON, supporting nested fields</li>
 * </ul>
 * The file is re-read on every query and rewritten as a whole on every write —
 * suited to config / dictionary scale data, not high-volume storage. A missing
 * file starts empty and is created on the first write. In list mode a missing
 * primary key is generated on add: numeric fields get max + 1, everything else a
 * UUID string. Single-record models should carry a primary key value in the file
 * so drill / operations can address the row.
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EruptFile {

    /**
     * File path, absolute or relative to the working directory, e.g. "data/dict.csv"
     */
    String value();

    /**
     * File format. Defaults to {@link FileType#AUTO} — inferred from the extension.
     * Set it explicitly when the path has no (or a misleading) extension, or to get
     * a clear error instead of a silent JSON fallback when a codec is unavailable.
     */
    FileType type() default FileType.AUTO;

    /**
     * Whether the file holds a single record (a pure settings / config form) rather
     * than a list. Only meaningful for JSON and YAML, whose top-level shape becomes
     * an object / mapping instead of an array / sequence. The {@code .properties}
     * and {@code .md} formats are always single-record regardless of this flag.
     */
    boolean single() default false;

}
