package xyz.erupt.cloud.node.handler;

import xyz.erupt.cloud.common.model.NodeInfo;

/**
 * Heartbeat extension point: beans implementing this interface are invoked before every
 * node registration, so optional modules can enrich the reported {@link NodeInfo} —
 * typically by putting a named list into {@link NodeInfo#getResources()}.
 *
 * @author YuePeng
 */
public interface NodeInfoHandler {

    void handle(NodeInfo nodeInfo);

}
