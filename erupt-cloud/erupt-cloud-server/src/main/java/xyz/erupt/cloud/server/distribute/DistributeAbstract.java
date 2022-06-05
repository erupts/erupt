package xyz.erupt.cloud.server.distribute;

import xyz.erupt.cloud.server.node.MetaNode;

/**
 * 分布式处理
 *
 * @author YuePeng
 * date 2022/3/8 21:25
 */
public abstract class DistributeAbstract {

    protected abstract void init();

    //集群节点更新
    protected abstract void distributePut(MetaNode metaNode);

    //集群节点移除
    protected abstract void distributeRemove(String nodeName);

    public void putNode(MetaNode metaNode) {
        this.distributePut(metaNode);
    }

    public void removeNode(String nodeName) {
        this.distributeRemove(nodeName);
    }

}
