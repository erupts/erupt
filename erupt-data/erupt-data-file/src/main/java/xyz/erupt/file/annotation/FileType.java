package xyz.erupt.file.annotation;

/**
 * File format for {@link EruptFile}. {@link #AUTO} infers the format from the file
 * extension; any other value pins it explicitly, which is useful when the path has
 * no (or a misleading) extension, or to surface a clear error when the backing
 * codec is unavailable (e.g. {@link #YAML} without SnakeYAML on the classpath).
 *
 * @author YuePeng
 */
public enum FileType {

    /**
     * Infer the format from the file extension.
     */
    AUTO,

    CSV,

    TSV,

    JSON,

    JSONL,

    YAML,

    PROPERTIES,

    INI,

    MARKDOWN,

    XML

}
