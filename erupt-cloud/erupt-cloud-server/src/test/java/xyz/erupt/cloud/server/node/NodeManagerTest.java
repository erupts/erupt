package xyz.erupt.cloud.server.node;

import org.junit.jupiter.api.Test;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeManager#pickLocations} only touches the round-robin cursor and the node's location set,
 * so it is exercised here without a Spring context / Redis.
 *
 * @author YuePeng
 */
class NodeManagerTest {

    private MetaNode nodeWith(String... locations) {
        MetaNode node = new MetaNode();
        node.setNodeName("demo");
        node.getLocations().addAll(List.of(locations));
        return node;
    }

    @Test
    void singleLocation_alwaysReturnsIt() {
        NodeManager manager = new NodeManager();
        MetaNode node = nodeWith("http://a");
        assertEquals(List.of("http://a"), manager.pickLocations(node));
        assertEquals(List.of("http://a"), manager.pickLocations(node));
    }

    @Test
    void multiLocation_rotatesHeadAndKeepsAllAsFailoverCandidates() {
        NodeManager manager = new NodeManager();
        MetaNode node = nodeWith("http://a", "http://b", "http://c");

        // First call starts the fresh cursor at 0, so it reflects the base ordering.
        List<String> base = manager.pickLocations(node);
        assertEquals(3, base.size());
        assertTrue(base.containsAll(List.of("http://a", "http://b", "http://c")),
                "every instance must remain as a failover candidate");

        // Subsequent calls rotate the primary pick through the ring.
        List<String> second = manager.pickLocations(node);
        List<String> third = manager.pickLocations(node);
        assertEquals(base.get(1), second.get(0));
        assertEquals(base.get(2), third.get(0));
        // A full rotation returns to the original head.
        assertEquals(base.get(0), manager.pickLocations(node).get(0));

        // Each ordering is a full rotation — same members, no drops.
        assertTrue(second.containsAll(base));
        assertTrue(third.containsAll(base));
    }

    @Test
    void noLocation_throws() {
        NodeManager manager = new NodeManager();
        MetaNode node = new MetaNode();
        node.setNodeName("demo");
        assertThrows(EruptWebApiRuntimeException.class, () -> manager.pickLocations(node));
    }

}
