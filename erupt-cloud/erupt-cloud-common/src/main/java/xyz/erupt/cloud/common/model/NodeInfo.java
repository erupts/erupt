package xyz.erupt.cloud.common.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Node 节点信息
 *
 * @author YuePeng
 * date 2022/1/28 22:23
 */
@Getter
@Setter
public class NodeInfo implements Serializable {

    private String instanceId;

    //版本
    private String version;

    //端口
    private Integer port;

    //节点名
    private String nodeName;

    //访问令牌
    private String accessToken;

    //应用上下文
    private String contextPath;

    //服务所管理的erupt清单
    private List<String> erupts = new ArrayList<>();

}
