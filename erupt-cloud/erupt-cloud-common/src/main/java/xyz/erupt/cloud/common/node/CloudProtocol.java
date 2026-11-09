package xyz.erupt.cloud.common.node;

import java.util.Arrays;

/**
 * The version of the node protocol an instance speaks (PROTOCOL.md §9, §12), sent as an integer at registration.
 *
 * @author YuePeng
 * date 2026/11/05
 */
public enum CloudProtocol {

    /**
     * Everything above PROTOCOL.md §12: the node checks what the server does not vouch for. A node that sends no
     * version speaks this one.
     */
    V1(1),

    /**
     * PROTOCOL.md §12, a thin node: the server completes and checks builds, enforces every right, checks and
     * normalizes every value and turns drills and link trees into conditions; the node stores and runs its code.
     */
    V2(2);

    private final int wire;

    CloudProtocol(int wire) {
        this.wire = wire;
    }

    public int wire() {
        return wire;
    }

    public static CloudProtocol latest() {
        return values()[values().length - 1];
    }

    /**
     * @param wire the version a node sent, or null for none
     * @return the version, V1 for none, null for a version this side does not know
     */
    public static CloudProtocol of(Integer wire) {
        if (null == wire) return V1;
        return Arrays.stream(values()).filter(it -> it.wire == wire).findFirst().orElse(null);
    }

}
