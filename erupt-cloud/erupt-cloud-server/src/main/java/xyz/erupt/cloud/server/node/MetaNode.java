package xyz.erupt.cloud.server.node;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.cloud.common.model.NodeInfo;

import java.io.Serializable;
import java.util.*;

/**
 * @author YuePeng
 * date 2021/12/22 00:12
 */
@Getter
@Setter
public class MetaNode extends NodeInfo implements Serializable {

    //服务注册时间
    private Date registerTime;

    //服务自动注册地址
    private Set<String> locations = new HashSet<>();

    //服务手动注册地址
    private List<String> manualLocations;

    //由erupts转换而来
    private transient Map<String, String> eruptMap = new HashMap<>();

    //调用次数
    private transient int count = 0;

    public int getCount() {
        return ++count;
    }

}
