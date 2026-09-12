package xyz.erupt.atlas.vo;

/**
 * One field of one model, flattened across the whole registry. The model view answers what depends
 * on what; this answers which column the change actually lands on.
 *
 * @author YuePeng
 */
public record FieldRow(String model, String label, String module, String name, String title,
                       String type, boolean required, boolean searchable, String reference) {
}
