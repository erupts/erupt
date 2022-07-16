package xyz.erupt.cloud.server.node;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.cloud.common.model.NodeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author YuePeng
 * date 2021/12/22 00:12
 */
@Getter
@Setter
public class MetaNode extends NodeInfo implements Serializable {

    public static final long serialVersionUID = -10086;

    //服务注册时间
    private Date registerTime;

    //服务自动注册地址
    private Set<String> locations = new HashSet<>();

}
