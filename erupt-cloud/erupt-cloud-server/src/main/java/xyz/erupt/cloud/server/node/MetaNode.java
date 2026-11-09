package xyz.erupt.cloud.server.node;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.cloud.common.model.NodeInfo;
import xyz.erupt.cloud.common.node.CloudProtocol;
import xyz.erupt.cloud.common.node.NodeCapability;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author YuePeng
 * date 2021/12/22 00:12
 */
@Getter
@Setter
public class MetaNode extends NodeInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -10086;

    //Service registration time
    private Date registerTime;

    //Service auto-registration addresses
    private Set<String> locations = new HashSet<>();

    /**
     * The version the node is served with: every instance of a node speaks the one it registered with
     * ({@link NodeInfo#getProtocolVersion()}), a node mixing versions is refused at registration.
     */
    public CloudProtocol protocol() {
        return Optional.ofNullable(CloudProtocol.of(getProtocolVersion())).orElse(CloudProtocol.V1);
    }

    public boolean thin() {
        return protocol() == CloudProtocol.V2;
    }

    // A v2 node runs all its code through invoke (PROTOCOL.md §12.1), whatever it lists
    public boolean invokes() {
        return can(NodeCapability.INVOKE) || thin();
    }

}
