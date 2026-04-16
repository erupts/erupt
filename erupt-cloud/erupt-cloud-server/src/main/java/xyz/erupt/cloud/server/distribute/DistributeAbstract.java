package xyz.erupt.cloud.server.distribute;

import xyz.erupt.cloud.server.node.MetaNode;

/**
 * distributed processing
 *
 * @author YuePeng
 * date 2022/3/8 21:25
 */
public abstract class DistributeAbstract {

    protected abstract void init();

    //Cluster node update
    protected abstract void distributePut(MetaNode metaNode);

    //Cluster node removal
    protected abstract void distributeRemove(String nodeName);

    public void putNode(MetaNode metaNode) {
        this.distributePut(metaNode);
    }

    public void removeNode(String nodeName) {
        this.distributeRemove(nodeName);
    }

}
