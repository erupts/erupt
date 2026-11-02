package xyz.erupt.cloud.common.node;

/**
 * An optional part of the node protocol a node says it serves when it registers (PROTOCOL.md §3.3).
 *
 * @author YuePeng
 * date 2026/10/21
 */
public enum NodeCapability {

    /**
     * {@code POST /erupt-api/invoke/{erupt}/{kind}/{field}}: the node runs its own code for a field's fetched
     * options, onchange, auto-complete and button, which the server then calls instead of the Java endpoints.
     */
    INVOKE;

    public String wire() {
        return name().toLowerCase();
    }

}
