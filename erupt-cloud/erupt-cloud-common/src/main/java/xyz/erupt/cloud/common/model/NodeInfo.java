package xyz.erupt.cloud.common.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

}
