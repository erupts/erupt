package xyz.erupt.cloud.common.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.cloud.common.node.NodeCapability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Node information
 *
 * @author YuePeng
 * date 2022/1/28 22:23
 */
@Getter
@Setter
public class NodeInfo implements Serializable {

    //Unique instance ID
    private String instanceId;

    //Version
    private String version;

    //Node name
    private String nodeName;

    //Access token
    private String accessToken;

    //Node address
    private String[] nodeAddress;

    //List of modules managed by the service
    private List<String> eruptModules = new ArrayList<>();

    //List of erupts managed by the service
    private List<String> erupts = new ArrayList<>();

    //Named resource lists contributed by optional modules (e.g. cube models), rendered on the server node page
    private Map<String, List<String>> resources = new LinkedHashMap<>();

    //Optional endpoints the node serves beyond the required ones, by NodeCapability wire name (PROTOCOL.md §3.3)
    private Set<String> capabilities = new LinkedHashSet<>();

    public boolean can(NodeCapability capability) {
        return null != capabilities && capabilities.contains(capability.wire());
    }

}
