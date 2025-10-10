package xyz.erupt.cloud.server.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.base.CloudServerConst;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.log.LogMessage;
import xyz.erupt.upms.annotation.EruptMenuAuth;
import xyz.erupt.upms.service.EruptContextService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * @author YuePeng
 * date 2023/6/9 22:10
 */
@RestController
@RequestMapping(CloudRestApiConst.ERUPT_CLOUD_API)
public class EruptNodeController {

    @Resource
    private NodeManager nodeManager;

    @Resource
    private EruptContextService eruptContextService;

    //移除实例
    @GetMapping("/remove-instance/{nodeName}")
    @EruptMenuAuth(CloudServerConst.CLOUD_NODE_MANAGER_PERMISSION)
    public void removeInstance(@PathVariable String nodeName, @RequestParam("instance") String instance) {
        nodeManager.removeNodeInstance(nodeName, instance);
    }

    //node节点日志
    @GetMapping("/erupt-cloud-node-log")
    @EruptMenuAuth(CloudServerConst.ERUPT_CLOUD_NODE_LOG)
    public List<LogMessage> eruptNodeLog(@RequestParam String nodeName, @RequestParam Long size, @RequestParam(required = false) Long offset,
                                         HttpServletResponse httpServletResponse) {
        MetaNode metaNode = nodeManager.getNode(nodeName);
        if (null == metaNode || metaNode.getLocations().isEmpty())
            throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
        try (HttpResponse httpResponse = HttpUtil.createGet(metaNode.getLocations().iterator().next() + EruptRestPath.ERUPT_TOOL + "/" + EruptConst.ERUPT_LOG)
                .header(EruptMutualConst.TOKEN, eruptContextService.getCurrentToken()).header(CloudCommonConst.HEADER_ACCESS_TOKEN, metaNode.getAccessToken())
                .form("size", size).form("offset", offset).execute()) {
            if (httpResponse.getStatus() == 200) {
                return GsonFactory.getGson().fromJson(httpResponse.body(), new TypeToken<List<LogMessage>>() {
                        }.getType()
                );
            } else {
                httpServletResponse.setStatus(httpResponse.getStatus());
                return null;
            }
        }
    }

}
