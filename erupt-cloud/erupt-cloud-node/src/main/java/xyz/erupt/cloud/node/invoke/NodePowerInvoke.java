package xyz.erupt.cloud.node.invoke;

import xyz.erupt.cloud.common.http.CloudHttp;
import org.springframework.web.client.RestClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.config.EruptNodeProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng
 * date 2022/2/20 01:06
 */
@Component
public class NodePowerInvoke implements PowerHandler {

    static {
        PowerInvoke.registerPowerHandler(NodePowerInvoke.class);
    }

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Resource
    private RestClient serverRestClient;

    @Override
    public void handler(PowerObject power) {
        EruptModel eruptModel = EruptCoreService.getErupt(MetaContext.getErupt().getName());
        String body = serverRestClient.get().uri(eruptNodeProp.getBalanceAddress() + CloudRestApiConst.ERUPT_POWER, builder -> builder
                        .queryParam("nodeName", eruptNodeProp.getNodeName())
                        .queryParam("eruptName", eruptModel.getEruptName()).build())
                .headers(CloudHttp.header(EruptMutualConst.TOKEN, MetaContext.getToken()))
                .retrieve().body(String.class);
        PowerObject remotePowerObject = GsonFactory.getGson().fromJson(body, PowerObject.class);
        if (power.isAdd()) power.setAdd(remotePowerObject.isAdd());
        if (power.isDelete()) power.setDelete(remotePowerObject.isDelete());
        if (power.isEdit()) power.setEdit(remotePowerObject.isEdit());
        if (power.isViewDetails()) power.setViewDetails(remotePowerObject.isViewDetails());
        if (power.isExport()) power.setExport(remotePowerObject.isExport());
        if (power.isImportable()) power.setImportable(remotePowerObject.isImportable());
    }

}
