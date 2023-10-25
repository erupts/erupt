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

    //实例唯一ID
    private String instanceId;

    //版本
    private String version;

    //节点名
    private String nodeName;

    //访问令牌
    private String accessToken;

    //node节点地址
    private String[] nodeAddress;

    //服务所管理的模块清单
    private List<String> eruptModules = new ArrayList<>();

    //服务所管理的erupt清单
    private List<String> erupts = new ArrayList<>();

}
