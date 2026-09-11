package xyz.erupt.graph.vo;

import java.util.List;

/**
 * Everything the registry knows about one model, fetched on demand: the graph payload carries
 * counts only, and field lists for 70 models would dwarf the graph itself.
 *
 * @author YuePeng
 */
public record ModelDetail(String name, String label, String desc, String source, String table,
                          String kind, boolean runtime, String primaryKey, List<String> power,
                          List<String> dataProxy, List<Field> fields, List<CubeItem> cubeItems) {

    /** reference is the erupt this field points at, null for a plain value field */
    public record Field(String name, String title, String type, boolean required, boolean searchable,
                        String reference) {
    }

    /** role: dimension | measure */
    public record CubeItem(String name, String title, String role, String type, String sql) {
    }

}
