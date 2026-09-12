package xyz.erupt.atlas.vo;

import java.util.List;

/**
 * A snapshot of the live erupt registry as a graph. Nothing here is persisted: every call
 * rebuilds from {@link xyz.erupt.core.service.EruptCoreService}, so models registered at
 * runtime (erupt-designer, erupt-flow forms) appear without a restart.
 *
 * @author YuePeng
 */
public record AtlasView(List<Node> nodes, List<Edge> edges, Audit audit) {

    /** kind: "erupt" (@Erupt model), "cube" (@EruptCube), "remote" (served by an erupt-cloud node) */
    public record Node(String id, String name, String label, String source, String kind,
                       boolean runtime, int fields, int dimensions, int measures, String table) {
    }

    /** kind: reference | tab | embed | drill | operation | cubeOf | join | table */
    public record Edge(String from, String to, String label, String kind) {
    }

    /**
     * What the graph knows that a picture does not say out loud: cycles to break, tables shared
     * by several models, and models nothing points at.
     */
    public record Audit(List<List<String>> cycles, List<SharedTable> sharedTables,
                        List<String> orphans, List<String> unpublished) {
    }

    public record SharedTable(String table, List<String> models) {
    }

}
